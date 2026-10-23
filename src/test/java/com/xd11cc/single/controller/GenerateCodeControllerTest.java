package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.vo.PreviewCodeVO;
import com.xd11cc.single.entity.vo.TableInfoQueryVO;
import com.xd11cc.single.entity.vo.TableInfoVO;
import com.xd11cc.single.service.GenerateCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenerateCodeControllerTest {

    @Mock
    private GenerateCodeService generateCodeService;

    @InjectMocks
    private GenerateCodeController generateCodeController;

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        TableInfoQueryVO vo = new TableInfoQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<TableInfoVO> tables = Collections.singletonList(buildTableInfo("sys_user", "用户表"));
        given(generateCodeService.list(vo)).willReturn(tables);

        ResponseVO<PageResult<TableInfoVO>> result = generateCodeController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
        assertThat(result.getData().getRows().get(0).getTableName()).isEqualTo("sys_user");
    }

    // ==================== generateCode ====================

    @Test
    void generateCode_生成代码_返回代码列表() {
        List<PreviewCodeVO> codes = Arrays.asList(
                buildPreviewCode("User.java", "public class User {}"),
                buildPreviewCode("UserMapper.java", "public interface UserMapper {}")
        );
        given(generateCodeService.generateCode("sys_user")).willReturn(codes);

        ResponseVO<List<PreviewCodeVO>> result = generateCodeController.generateCode("sys_user");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getFileName()).isEqualTo("User.java");
    }

    // ==================== 辅助方法 ====================

    private static TableInfoVO buildTableInfo(String tableName, String tableComment) {
        TableInfoVO vo = new TableInfoVO();
        vo.setTableName(tableName);
        vo.setTableComment(tableComment);
        return vo;
    }

    private static PreviewCodeVO buildPreviewCode(String fileName, String content) {
        return new PreviewCodeVO(fileName, content);
    }
}
