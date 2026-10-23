package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemOperateLogDO;
import com.xd11cc.single.entity.vo.SystemOperateLogQueryVO;
import com.xd11cc.single.service.ISystemOperateLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SystemOperateLogControllerTest {

    @Mock
    private ISystemOperateLogService systemOperateLogService;

    @InjectMocks
    private SystemOperateLogController operateLogController;

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemOperateLogQueryVO vo = new SystemOperateLogQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemOperateLogDO> logs = Collections.singletonList(buildOperateLog(1L, "用户管理", "新增", "新增用户"));
        given(systemOperateLogService.getList(vo)).willReturn(logs);

        ResponseVO<PageResult<SystemOperateLogDO>> result = operateLogController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemOperateLogService.deleteByIds(java.util.Collections.singletonList(1L))).willReturn(1);

        ResponseVO<Integer> result = operateLogController.removeByIds(java.util.Collections.singletonList(1L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== clean ====================

    @Test
    void clean_清空操作日志_返回成功() {
        ResponseVO<Void> result = operateLogController.clean();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    // ==================== 辅助方法 ====================

    private static SystemOperateLogDO buildOperateLog(Long id, String module, String operateType, String operateDesc) {
        SystemOperateLogDO log = new SystemOperateLogDO();
        log.setId(id);
        log.setModule(module);
        log.setOperateType(operateType);
        log.setOperateDesc(operateDesc);
        return log;
    }
}
