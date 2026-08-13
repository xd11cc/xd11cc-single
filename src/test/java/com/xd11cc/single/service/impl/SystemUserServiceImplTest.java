package com.xd11cc.single.service.impl;

import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.domain.SystemUserRoleDO;
import com.xd11cc.single.entity.vo.SystemUserAddVO;
import com.xd11cc.single.entity.vo.SystemUserDetailVO;
import com.xd11cc.single.entity.vo.SystemUserQueryVO;
import com.xd11cc.single.entity.vo.SystemUserUpdateVO;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.mapper.SystemUserMapper;
import com.xd11cc.single.service.ISystemUserRoleService;
import com.xd11cc.single.service.ISystemUserService;
import com.xd11cc.single.util.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SystemUserServiceImplTest extends BaseUnitTest {

    private static final String RAW_PASSWORD = "pass123456";
    private static final String ENCODED_PASSWORD = new BCryptPasswordEncoder().encode(RAW_PASSWORD);

    @Mock
    private SystemUserMapper baseMapper;
    @Mock
    private ISystemUserRoleService systemUserRoleService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SystemUserServiceImpl userService;

    // ==================== getByUsername ====================

    @Test
    void getByUsername_存在_返回用户() {
        SystemUserDO expected = buildUser(1L, "admin", "admin@test.com");
        given(baseMapper.selectOne(any())).willReturn(expected);

        SystemUserDO result = userService.getByUsername("admin");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getByUsername_不存在_返回null() {
        given(baseMapper.selectOne(any())).willReturn(null);

        SystemUserDO result = userService.getByUsername("nonexistent");

        assertThat(result).isNull();
    }

    // ==================== getByEmail ====================

    @Test
    void getByEmail_存在_返回用户() {
        SystemUserDO expected = buildUser(2L, "user", "user@test.com");
        expected.setEmail("user@test.com");
        given(baseMapper.selectOne(any())).willReturn(expected);

        SystemUserDO result = userService.getByEmail("user@test.com");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getByEmail_不存在_返回null() {
        given(baseMapper.selectOne(any())).willReturn(null);

        SystemUserDO result = userService.getByEmail("no@test.com");

        assertThat(result).isNull();
    }

    // ==================== add ====================

    @Test
    void add_成功_加密密码并保存角色关联() {
        SystemUserAddVO vo = buildAddVO("newuser", RAW_PASSWORD, "新用户");
        SystemUserDO userDO = buildUser(1L, "newuser", "new@test.com");
        userDO.setPassword(ENCODED_PASSWORD);
        given(baseMapper.insert(any(SystemUserDO.class))).willReturn(1);
        given(systemUserRoleService.saveBatch(anyList())).willReturn(true);

        int row = userService.add(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().insert(any(SystemUserDO.class));
        then(systemUserRoleService).should().saveBatch(anyList());
    }

    @Test
    void add_重复键_抛用户名已存在() {
        SystemUserAddVO vo = buildAddVO("dupuser", RAW_PASSWORD, "重复");
        willThrow(new DuplicateKeyException("dup")).given(baseMapper).insert(any(SystemUserDO.class));

        ServiceException ex = assertThrows(ServiceException.class, () -> userService.add(vo));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.USERNAME_EXISTS);
        then(systemUserRoleService).should(org.mockito.BDDMockito.never()).saveBatch(anyList());
    }

    // ==================== deleteByIds ====================

    @Test
    void deleteByIds_包含管理员_抛不允许删除管理员() {
        List<Long> ids = Arrays.asList(1L, 2L);

        ServiceException ex = assertThrows(ServiceException.class, () -> userService.deleteByIds(ids));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.ADMIN_NOT_ALLOW_DELETE);
        then(baseMapper).should(org.mockito.BDDMockito.never()).deleteBatchIds(any());
    }

    @Test
    void deleteByIds_不包含管理员_删除并清理角色() {
        given(baseMapper.deleteBatchIds(Arrays.asList(2L, 3L))).willReturn(2);
        given(systemUserRoleService.remove(any())).willReturn(true);

        int row = userService.deleteByIds(Arrays.asList(2L, 3L));

        assertThat(row).isEqualTo(2);
        then(baseMapper).should().deleteBatchIds(Arrays.asList(2L, 3L));
        then(systemUserRoleService).should().remove(any());
    }

    // ==================== modifyById ====================

    @Test
    void modifyById_成功_更新用户并更新角色() {
        SystemUserUpdateVO vo = buildUpdateVO(1L, "新昵称", "new@test.com", "1", 10L);
        given(baseMapper.updateById(any(SystemUserDO.class))).willReturn(1);
        given(systemUserRoleService.remove(any())).willReturn(true);
        given(systemUserRoleService.saveBatch(anyList())).willReturn(true);

        int row = userService.modifyById(vo);

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemUserDO.class));
        then(systemUserRoleService).should().remove(any());
        then(systemUserRoleService).should().saveBatch(anyList());
    }

    // ==================== getList ====================

    @Test
    void getList_带筛选条件_构建查询() {
        SystemUserQueryVO vo = new SystemUserQueryVO();
        vo.setUsername("admin");
        vo.setNickname("管理员");
        vo.setStatus("0");
        List<SystemUserDO> expected = Collections.singletonList(buildUser(1L, "admin", "admin@test.com"));
        given(baseMapper.selectList(any())).willReturn(expected);

        List<SystemUserDO> result = userService.getList(vo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("admin");
    }

    @Test
    void getList_空条件_返回全部() {
        SystemUserQueryVO vo = new SystemUserQueryVO();
        SystemUserDO u1 = buildUser(1L, "a", "a@test.com");
        SystemUserDO u2 = buildUser(2L, "b", "b@test.com");
        given(baseMapper.selectList(any())).willReturn(Arrays.asList(u1, u2));

        List<SystemUserDO> result = userService.getList(vo);

        assertThat(result).hasSize(2);
    }

    // ==================== resetPassword ====================

    @Test
    void resetPassword_成功_更新密码() {
        given(baseMapper.updateById(any(SystemUserDO.class))).willReturn(1);

        int row = userService.resetPassword(1L, "new_password");

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemUserDO.class));
    }

    // ==================== getDetailById ====================

    @Test
    void getDetailById_存在_返回详情含角色() {
        SystemUserDO user = buildUser(1L, "admin", "admin@test.com");
        user.setCreateTime(new Date());
        given(baseMapper.selectById(1L)).willReturn(user);
        SystemUserDetailVO detailVO = new SystemUserDetailVO();
        detailVO.setId(1L);
        detailVO.setUsername("admin");
        // 用 spy 模拟 MapStruct 转换
        SystemUserDetailVO spyVO = org.mockito.Mockito.spy(detailVO);
        // 由于 MapStruct 是真实调用，我们验证 roleIds 被设置

        SystemUserRoleDO ur1 = buildUserRole(1L, 1L, 10L);
        SystemUserRoleDO ur2 = buildUserRole(2L, 1L, 20L);
        given(systemUserRoleService.list(any())).willReturn(Arrays.asList(ur1, ur2));

        SystemUserDetailVO result = userService.getDetailById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRoleIds()).containsExactly(10L, 20L);
    }

    @Test
    void getDetailById_不存在_返回null() {
        given(baseMapper.selectById(99L)).willReturn(null);

        SystemUserDetailVO result = userService.getDetailById(99L);

        assertThat(result).isNull();
        then(systemUserRoleService).should(org.mockito.BDDMockito.never()).list(any());
    }

    // ==================== changePassword ====================

    @Test
    void changePassword_旧密码正确_更新成功() {
        SystemUserDO user = buildUser(1L, "admin", "admin@test.com");
        user.setPassword(ENCODED_PASSWORD);
        given(baseMapper.selectById(1L)).willReturn(user);
        given(baseMapper.updateById(any(SystemUserDO.class))).willReturn(1);

        int row = userService.changePassword(1L, RAW_PASSWORD, "new_password");

        assertThat(row).isEqualTo(1);
        then(baseMapper).should().updateById(any(SystemUserDO.class));
    }

    @Test
    void changePassword_旧密码错误_抛异常() {
        SystemUserDO user = buildUser(1L, "admin", "admin@test.com");
        user.setPassword(ENCODED_PASSWORD);
        given(baseMapper.selectById(1L)).willReturn(user);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userService.changePassword(1L, "wrong_password", "new_password"));
        assertThat(ex.getErrorCode()).isEqualTo(SystemErrorEnum.OLD_PASSWORD_ERROR);
        then(baseMapper).should(org.mockito.BDDMockito.never()).updateById(any());
    }

    // ==================== 辅助方法 ====================

    private static SystemUserDO buildUser(Long id, String username, String email) {
        SystemUserDO user = new SystemUserDO();
        user.setId(id);
        user.setUsername(username);
        user.setNickname("nick_" + username);
        user.setEmail(email);
        user.setPhone("13800138000");
        user.setSex("1");
        user.setStatus("0");
        user.setDeptId(1L);
        user.setDeptName("技术部");
        user.setPostId(1L);
        user.setPostName("Java开发");
        user.setPassword(ENCODED_PASSWORD);
        user.setHeadUrl("/avatar/default.png");
        user.setRemark("备注");
        return user;
    }

    private static SystemUserAddVO buildAddVO(String username, String password, String nickname) {
        SystemUserAddVO vo = new SystemUserAddVO();
        vo.setUsername(username);
        vo.setPassword(password);
        vo.setNickname(nickname);
        vo.setPhone("13800138000");
        vo.setEmail(username + "@test.com");
        vo.setSex("1");
        vo.setDeptId(1L);
        vo.setDeptName("技术部");
        vo.setPostId(1L);
        vo.setPostName("Java开发");
        vo.setRoleIds(Arrays.asList(10L, 20L));
        vo.setStatus("0");
        vo.setRemark("备注");
        return vo;
    }

    private static SystemUserUpdateVO buildUpdateVO(Long id, String nickname, String email, String sex, Long deptId) {
        SystemUserUpdateVO vo = new SystemUserUpdateVO();
        vo.setId(id);
        vo.setNickname(nickname);
        vo.setPhone("13800138000");
        vo.setEmail(email);
        vo.setSex(sex);
        vo.setDeptId(deptId);
        vo.setDeptName("技术部");
        vo.setPostId(1L);
        vo.setPostName("Java开发");
        vo.setRoleIds(Arrays.asList(10L, 20L));
        vo.setStatus("0");
        vo.setRemark("备注");
        return vo;
    }

    private static SystemUserRoleDO buildUserRole(Long id, Long userId, Long roleId) {
        SystemUserRoleDO ur = new SystemUserRoleDO();
        ur.setId(id);
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        ur.setRemark("关联");
        return ur;
    }
}
