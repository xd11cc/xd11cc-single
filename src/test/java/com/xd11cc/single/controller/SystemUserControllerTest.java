package com.xd11cc.single.controller;

import com.xd11cc.single.entity.base.PageResult;
import com.xd11cc.single.entity.base.ResponseVO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.SystemUserAddVO;
import com.xd11cc.single.entity.vo.SystemUserChangePasswordVO;
import com.xd11cc.single.entity.vo.SystemUserDetailVO;
import com.xd11cc.single.entity.vo.SystemUserQueryVO;
import com.xd11cc.single.entity.vo.SystemUserUpdateVO;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SystemUserControllerTest {

    @Mock
    private ISystemUserService systemUserService;

    @InjectMocks
    private SystemUserController userController;

    // ==================== add ====================

    @Test
    void add_成功_返回行数() {
        SystemUserAddVO vo = buildAddVO("admin", "pass123", "管理员", "74955953432", "admin@xd11cc.com", "1", 1L, "研发部", 1L, "开发岗", Arrays.asList(1L, 2L), "0", "备注");
        given(systemUserService.add(vo)).willReturn(1);

        ResponseVO<Integer> result = userController.add(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("新增成功");
    }

    @Test
    void add_失败_返回失败消息() {
        SystemUserAddVO vo = buildAddVO("admin", "pass123", "管理员", "74955953432", "admin@xd11cc.com", "1", 1L, "研发部", 1L, "开发岗", Arrays.asList(1L, 2L), "0", "备注");
        given(systemUserService.add(vo)).willReturn(0);

        ResponseVO<Integer> result = userController.add(vo);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getData()).isNull();
        assertThat(result.getMsg()).isEqualTo("新增失败");
    }

    // ==================== removeByIds ====================

    @Test
    void removeByIds_成功_返回删除行数() {
        given(systemUserService.deleteByIds(Arrays.asList(1L, 2L))).willReturn(2);

        ResponseVO<Integer> result = userController.removeByIds(Arrays.asList(1L, 2L));

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(2);
        assertThat(result.getMsg()).isEqualTo("删除成功");
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_返回行数() {
        SystemUserUpdateVO vo = buildUpdateVO(1L, "新昵称", "74955953432", "new@xd11cc.com", "1", 1L, "研发部", 1L, "开发岗", Arrays.asList(1L, 2L), "0", "备注");
        given(systemUserService.modifyById(vo)).willReturn(1);

        ResponseVO<Integer> result = userController.modifyById(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("修改成功");
    }

    // ==================== page ====================

    @Test
    void page_查询_返回分页结果() {
        SystemUserQueryVO vo = new SystemUserQueryVO();
        vo.setCurrentPage(1);
        vo.setPageSize(10);
        List<SystemUserDO> userList = Collections.singletonList(buildUser(1L, "admin", "管理员", "0"));
        given(systemUserService.getList(vo)).willReturn(userList);

        ResponseVO<PageResult<SystemUserDO>> result = userController.page(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRows()).hasSize(1);
    }

    // ==================== list ====================

    @Test
    void list_查询所有用户() {
        SystemUserQueryVO vo = new SystemUserQueryVO();
        List<SystemUserDO> users = Arrays.asList(buildUser(1L, "admin", "管理员", "0"), buildUser(2L, "user", "用户", "0"));
        given(systemUserService.getList(vo)).willReturn(users);

        ResponseVO<List<SystemUserDO>> result = userController.list(vo);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getUsername()).isEqualTo("admin");
    }

    // ==================== getById ====================

    @Test
    void getById_存在_返回用户详情() {
        SystemUserDetailVO detail = new SystemUserDetailVO();
        detail.setId(1L);
        detail.setUsername("admin");
        given(systemUserService.getDetailById(1L)).willReturn(detail);

        ResponseVO<SystemUserDetailVO> result = userController.getById(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getUsername()).isEqualTo("admin");
    }

    // ==================== resetPassword ====================

    @Test
    void resetPassword_成功_返回行数() {
        given(systemUserService.resetPassword(1L, "newPass123")).willReturn(1);

        ResponseVO<Integer> result = userController.resetPassword(1L, "newPass123");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo(1);
        assertThat(result.getMsg()).isEqualTo("重置成功");
    }

    // ==================== changePassword ====================

    @Test
    void changePassword_成功_返回行数() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getUserId).thenReturn(1L);
            given(systemUserService.changePassword(1L, "oldPass", "newPass")).willReturn(1);

            SystemUserChangePasswordVO vo = new SystemUserChangePasswordVO();
            vo.setOldPassword("oldPass");
            vo.setNewPassword("newPass");
            ResponseVO<Integer> result = userController.changePassword(vo);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).isEqualTo(1);
            assertThat(result.getMsg()).isEqualTo("修改成功");
        }
    }

    // ==================== 辅助方法 ====================

    private static SystemUserAddVO buildAddVO(String username, String password, String nickname,
                                              String phone, String email, String sex,
                                              Long deptId, String deptName, Long postId, String postName,
                                              List<Long> roleIds, String status, String remark) {
        SystemUserAddVO vo = new SystemUserAddVO();
        vo.setUsername(username);
        vo.setPassword(password);
        vo.setNickname(nickname);
        vo.setPhone(phone);
        vo.setEmail(email);
        vo.setSex(sex);
        vo.setDeptId(deptId);
        vo.setDeptName(deptName);
        vo.setPostId(postId);
        vo.setPostName(postName);
        vo.setRoleIds(roleIds);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemUserUpdateVO buildUpdateVO(Long id, String nickname, String phone, String email,
                                                     String sex, Long deptId, String deptName, Long postId,
                                                     String postName, List<Long> roleIds, String status, String remark) {
        SystemUserUpdateVO vo = new SystemUserUpdateVO();
        vo.setId(id);
        vo.setNickname(nickname);
        vo.setPhone(phone);
        vo.setEmail(email);
        vo.setSex(sex);
        vo.setDeptId(deptId);
        vo.setDeptName(deptName);
        vo.setPostId(postId);
        vo.setPostName(postName);
        vo.setRoleIds(roleIds);
        vo.setStatus(status);
        vo.setRemark(remark);
        return vo;
    }

    private static SystemUserDO buildUser(Long id, String username, String nickname, String status) {
        SystemUserDO user = new SystemUserDO();
        user.setId(id);
        user.setUsername(username);
        user.setNickname(nickname);
        user.setStatus(status);
        return user;
    }
}
