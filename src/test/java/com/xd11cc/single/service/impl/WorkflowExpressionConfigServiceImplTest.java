package com.xd11cc.single.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.entity.domain.WorkflowExpressionConfigDO;
import com.xd11cc.single.entity.vo.WorkflowExpressionConfigAddVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.infrastructure.redis.RedisCache;
import com.xd11cc.single.mapper.WorkflowExpressionConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * 工作流表达式配置的占位符解析与校验。
 * <p>
 * 这里钉住三件容易出问题的事：
 * 1. 三种模板写法（{key} / {{key}} / 带 ${} 外壳）都能解析出同样的结果；
 * 2. 模板里的占位符必须与 parameter_schema 声明的参数一一对应，否则保存失败
 *    ——历史上有条「单花括号 + 空 schema」的配置因为识别不到占位符而静默入库；
 * 3. 解析结果会补上 Flowable 需要的 ${...} 外壳。
 * </p>
 *
 * @author xd11cc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowExpressionConfigServiceImplTest {

    private static final String SCHEMA =
            "[{\"key\":\"dayVariable\",\"type\":\"PROCESS_VARIABLE\",\"required\":true},"
                    + "{\"key\":\"threshold\",\"type\":\"NUMBER\",\"required\":true}]";
    private static final String PARAMS = "{\"dayVariable\":\"leaveDays\",\"threshold\":3}";

    @Mock
    private WorkflowExpressionConfigMapper baseMapper;

    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private WorkflowExpressionConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
    }

    private void givenConfig(String template, String schema) {
        WorkflowExpressionConfigDO config = new WorkflowExpressionConfigDO();
        config.setExpressionTemplate(template);
        config.setParameterSchema(schema);
        given(baseMapper.selectOne(any())).willReturn(config);
    }

    // ==================== 解析：三种写法等价 ====================

    @Test
    @DisplayName("推荐写法 {key}：解析出 ${leaveDays > 3}")
    void resolve_singleBrace() {
        givenConfig("{dayVariable} > {threshold}", SCHEMA);

        assertThat(service.resolveExpression("condition.day-gte-threshold", PARAMS))
                .isEqualTo("${leaveDays > 3}");
    }

    @Test
    @DisplayName("旧写法 {{key}} 保持兼容")
    void resolve_doubleBrace() {
        givenConfig("${{{dayVariable}} > {{threshold}}}", SCHEMA);

        assertThat(service.resolveExpression("condition.day-gte-threshold", PARAMS))
                .isEqualTo("${leaveDays > 3}");
    }

    @Test
    @DisplayName("手写 ${} 外壳 + 单花括号占位符也能识别")
    void resolve_singleBraceInsideWrapper() {
        givenConfig("${{dayVariable} > {threshold}}", SCHEMA);

        assertThat(service.resolveExpression("condition.day-gte-threshold", PARAMS))
                .isEqualTo("${leaveDays > 3}");
    }

    @Test
    @DisplayName("只有变量没有运算符：补外壳")
    void resolve_singleVariable() {
        givenConfig("{initiatorVariable}",
                "[{\"key\":\"initiatorVariable\",\"type\":\"PROCESS_VARIABLE\",\"required\":true}]");

        assertThat(service.resolveExpression("assignee.initiator",
                "{\"initiatorVariable\":\"initiator\"}"))
                .isEqualTo("${initiator}");
    }

    // ==================== 解析：异常路径 ====================

    @Test
    @DisplayName("未声明的占位符：直接当作同名流程变量")
    void resolve_undeclaredPlaceholderAsVariable() {
        givenConfig("{dayVariable} > 3", "[]");

        assertThat(service.resolveExpression("x", "{}"))
                .isEqualTo("${dayVariable > 3}");
    }

    @Test
    @DisplayName("必填参数缺失：抛 EXPRESSION_PARAM_MISSING")
    void resolve_missingRequiredParam() {
        givenConfig("{dayVariable} > {threshold}", SCHEMA);

        assertThatThrownBy(() -> service.resolveExpression("x", "{\"dayVariable\":\"leaveDays\"}"))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.EXPRESSION_PARAM_MISSING));
    }

    // ==================== 保存校验 ====================

    @Test
    @DisplayName("声明的参数没在模板里用到：保存失败")
    void add_declaredParameterNotUsed() {
        WorkflowExpressionConfigAddVO vo = buildVO("{dayVariable} > 3",
                "[{\"key\":\"threshold\",\"type\":\"NUMBER\",\"required\":true}]");

        assertThatThrownBy(() -> service.add(vo))
                .isInstanceOf(ServiceException.class)
                .satisfies(e -> assertThat(((ServiceException) e).getErrorCode())
                        .isEqualTo(SystemErrorEnum.EXPRESSION_PLACEHOLDER_MISMATCH));
    }

    @Test
    @DisplayName("空 schema 的模板：占位符按变量名处理，允许保存")
    void add_emptySchema_treatedAsVariables() {
        WorkflowExpressionConfigAddVO vo = buildVO("{dayVariable} > 3", "[]");
        given(baseMapper.insert(any(WorkflowExpressionConfigDO.class))).willReturn(1);

        assertThat(service.add(vo)).isEqualTo(1);
    }

    @Test
    @DisplayName("参数与占位符匹配：允许保存")
    void add_matchingSchema() {
        WorkflowExpressionConfigAddVO vo = buildVO("{dayVariable} > {threshold}", SCHEMA);
        given(baseMapper.insert(any(WorkflowExpressionConfigDO.class))).willReturn(1);

        assertThat(service.add(vo)).isEqualTo(1);
    }

    private WorkflowExpressionConfigAddVO buildVO(String template, String schema) {
        WorkflowExpressionConfigAddVO vo = new WorkflowExpressionConfigAddVO();
        vo.setConfigKey("condition.day-gte-threshold");
        vo.setConfigName("请假达到审批阈值");
        vo.setExpressionType("SEQUENCE_CONDITION");
        vo.setReturnType("BOOLEAN");
        vo.setExpressionTemplate(template);
        vo.setParameterSchema(schema);
        return vo;
    }
}
