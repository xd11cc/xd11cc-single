package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.config.properties.TenantIgnoreProperties;
import com.xd11cc.single.entity.dto.GenerateConfigDTO;
import com.xd11cc.single.entity.vo.ColumnInfoVO;
import com.xd11cc.single.entity.vo.PreviewCodeVO;
import com.xd11cc.single.entity.vo.TableInfoQueryVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.GenerateCodeMapper;
import com.xd11cc.single.utils.GeneratorUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenerateCodeServiceImplTest {

    @Mock
    private GenerateCodeMapper generateCodeMapper;

    @Mock
    private TenantIgnoreProperties tenantIgnoreProperties;

    @InjectMocks
    private GenerateCodeServiceImpl generateCodeService;

    // ==================== list ====================

    @Test
    void list_委托给generateCodeMapper_selectList() {
        TableInfoQueryVO queryVO = new TableInfoQueryVO();
        queryVO.setTableName("sys_user");

        List<com.xd11cc.single.entity.vo.TableInfoVO> mockResult =
                Collections.singletonList(new com.xd11cc.single.entity.vo.TableInfoVO());
        given(generateCodeMapper.selectList(queryVO)).willReturn(mockResult);

        List<com.xd11cc.single.entity.vo.TableInfoVO> result = generateCodeService.list(queryVO);

        assertThat(result).hasSize(1);
    }

    // ==================== generateCode 空列列表 ====================

    @Test
    void generateCode_空列列表_抛GENERATE_CODE_ERROR() {
        given(generateCodeMapper.selectByTableName("sys_user"))
                .willReturn(Collections.emptyList());

        assertThatThrownBy(() -> generateCodeService.generateCode("sys_user"))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> {
                    ServiceException se = (ServiceException) ex;
                    assertThat(se.getErrorCode())
                            .isEqualTo(SystemErrorEnum.GENERATE_CODE_ERROR);
                });
    }

    // ==================== generateCode 非租户表 ====================

    @Test
    void generateCode_忽略表_hasTenant为false() {
        ColumnInfoVO column = new ColumnInfoVO();
        column.setColumnName("id");
        column.setColumnComment("主键");
        column.setColumnKey("PRI");
        column.setDataType("bigint");
        column.setIsNullable("NO");
        column.setExtra("auto_increment");

        given(generateCodeMapper.selectByTableName("sys_dict_type"))
                .willReturn(Collections.singletonList(column));
        given(tenantIgnoreProperties.getIgnoreTables())
                .willReturn(Collections.singletonList("sys_dict_type"));

        try (org.mockito.MockedStatic<GeneratorUtil> mocked = org.mockito.Mockito.mockStatic(GeneratorUtil.class)) {
            mocked.when(() -> GeneratorUtil.generatorCode(
                    org.mockito.ArgumentMatchers.anyList(),
                    org.mockito.ArgumentMatchers.any(GenerateConfigDTO.class)))
                    .thenReturn(Collections.emptyList());

            List<PreviewCodeVO> result = generateCodeService.generateCode("sys_dict_type");

            assertThat(result).isEmpty();
            mocked.verify(() -> GeneratorUtil.generatorCode(
                    org.mockito.ArgumentMatchers.anyList(),
                    org.mockito.ArgumentMatchers.any(GenerateConfigDTO.class)));
        }
    }
}
