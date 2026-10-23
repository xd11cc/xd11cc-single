package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemLoginLogDO;
import com.xd11cc.single.entity.vo.SystemLoginLogQueryVO;
import com.xd11cc.single.service.ISystemLoginLogService;
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
class SystemLoginLogControllerTest {

    @Mock
    private ISystemLoginLogService systemLoginLogService;

    @InjectMocks
    private SystemLoginLogController loginLogController;

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemLoginLogQueryVO vo = new SystemLoginLogQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemLoginLogDO> logs = Collections.singletonList(buildLoginLog(1L, "admin", "登录成功"));
        given(systemLoginLogService.getList(vo)).willReturn(logs);

        ResponseVO<PageResult<SystemLoginLogDO>> result = loginLogController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemLoginLogService.deleteByIds(java.util.Collections.singletonList(1L))).willReturn(1);

        ResponseVO<Integer> result = loginLogController.removeByIds(java.util.Collections.singletonList(1L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== clean ====================

    @Test
    void clean_清空登录日志_返回成功() {
        ResponseVO<Void> result = loginLogController.clean();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    // ==================== 辅助方法 ====================

    private static SystemLoginLogDO buildLoginLog(Long id, String username, String msg) {
        SystemLoginLogDO log = new SystemLoginLogDO();
        log.setId(id);
        log.setUsername(username);
        log.setMsg(msg);
        return log;
    }
}
