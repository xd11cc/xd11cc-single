package com.xd11cc.single.convert;

import com.xd11cc.single.entity.domain.AuthClientConfigDO;
import com.xd11cc.single.entity.domain.SystemConfigDO;
import com.xd11cc.single.entity.domain.SystemDeptDO;
import com.xd11cc.single.entity.domain.SystemDictDataDO;
import com.xd11cc.single.entity.domain.SystemDictTypeDO;
import com.xd11cc.single.entity.domain.SystemMenuDO;
import com.xd11cc.single.entity.domain.SystemNoticeDO;
import com.xd11cc.single.entity.domain.SystemPostDO;
import com.xd11cc.single.entity.domain.SystemRoleDO;
import com.xd11cc.single.entity.domain.SystemTenantDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.vo.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ConvertTest {

    // ==================== SystemUserConvert ====================

    @Test
    void systemUserConvert_do2vo_字段全映射() {
        SystemUserDO userDO = buildUserDO();
        UserLoginInfoVO vo = SystemUserConvert.INSTANCE.do2vo(userDO);

        assertCommonUserFields(vo);
        assertThat(vo.getRoles()).isNull();
        assertThat(vo.getRoleIds()).isNull();
        assertThat(vo.getRoleNames()).isNull();
        assertThat(vo.getPermissions()).isNull();
    }

    @Test
    void systemUserConvert_addVO2DO_字段全映射() {
        SystemUserAddVO addVO = new SystemUserAddVO();
        addVO.setUsername("newuser");
        addVO.setPassword("pass123");
        addVO.setNickname("新用户");
        addVO.setPhone("74955953457");
        addVO.setEmail("user@test.com");
        addVO.setSex("1");
        addVO.setDeptId(2L);
        addVO.setPostId(3L);
        addVO.setStatus("0");
        addVO.setRemark("测试");

        SystemUserDO userDO = SystemUserConvert.INSTANCE.addVO2DO(addVO);

        assertThat(userDO.getUsername()).isEqualTo("newuser");
        assertThat(userDO.getPassword()).isEqualTo("pass123");
        assertThat(userDO.getNickname()).isEqualTo("新用户");
        assertThat(userDO.getPhone()).isEqualTo("74955953457");
        assertThat(userDO.getEmail()).isEqualTo("user@test.com");
        assertThat(userDO.getSex()).isEqualTo("1");
        assertThat(userDO.getDeptId()).isEqualTo(2L);
        assertThat(userDO.getPostId()).isEqualTo(3L);
        assertThat(userDO.getStatus()).isEqualTo("0");
        assertThat(userDO.getRemark()).isEqualTo("测试");
    }

    @Test
    void systemUserConvert_updateVO2DO_字段全映射() {
        SystemUserUpdateVO updateVO = new SystemUserUpdateVO();
        updateVO.setId(10L);
        updateVO.setNickname("更新昵称");
        updateVO.setPhone("74955953457");
        updateVO.setEmail("update@test.com");
        updateVO.setSex("1");
        updateVO.setStatus("1");

        SystemUserDO userDO = SystemUserConvert.INSTANCE.updateVO2DO(updateVO);

        assertThat(userDO.getId()).isEqualTo(10L);
        assertThat(userDO.getNickname()).isEqualTo("更新昵称");
        assertThat(userDO.getPhone()).isEqualTo("74955953457");
        assertThat(userDO.getEmail()).isEqualTo("update@test.com");
        assertThat(userDO.getSex()).isEqualTo("1");
        assertThat(userDO.getStatus()).isEqualTo("1");
    }

    @Test
    void systemUserConvert_do2DetailVO_含roleIds和createTime() {
        SystemUserDO userDO = buildUserDO();
        userDO.setCreateTime(new Date(1700000000000L));

        SystemUserDetailVO vo = SystemUserConvert.INSTANCE.do2DetailVO(userDO);

        assertThat(vo.getId()).isEqualTo(88L);
        assertThat(vo.getUsername()).isEqualTo("testuser");
        assertThat(vo.getNickname()).isEqualTo("测试用户");
        assertThat(vo.getSex()).isEqualTo("1");
        assertThat(vo.getPhone()).isEqualTo("74955953457");
        assertThat(vo.getEmail()).isEqualTo("test@xd11cc.com");
        assertThat(vo.getDeptId()).isEqualTo(10L);
        assertThat(vo.getDeptName()).isEqualTo("技术部");
        assertThat(vo.getPostId()).isEqualTo(1L);
        assertThat(vo.getPostName()).isEqualTo("开发工程师");
        assertThat(vo.getStatus()).isEqualTo("0");
        assertThat(vo.getHeadUrl()).isEqualTo("/avatar.png");
        assertThat(vo.getRemark()).isEqualTo("备注信息");
        assertThat(vo.getRoleIds()).isNull();
        assertThat(vo.getCreateTime()).isEqualTo(new Date(1700000000000L));
    }

    // ==================== SystemMenuConvert ====================

    @Test
    void systemMenuConvert_addVO2DO_字段全映射() {
        SystemMenuAddVO addVO = new SystemMenuAddVO();
        addVO.setParentId(0L);
        addVO.setMenuName("测试菜单");
        addVO.setSort(1);
        addVO.setPath("/test");
        addVO.setComponent("TestPage");
        addVO.setMenuType("C");
        addVO.setVisible("0");
        addVO.setStatus("0");
        addVO.setPermission("test:view");
        addVO.setIcon("el-icon-menu");
        addVO.setRemark("菜单备注");

        SystemMenuDO menuDO = SystemMenuConvert.INSTANCE.addVO2DO(addVO);

        assertThat(menuDO.getParentId()).isEqualTo(0L);
        assertThat(menuDO.getMenuName()).isEqualTo("测试菜单");
        assertThat(menuDO.getSort()).isEqualTo(1);
        assertThat(menuDO.getPath()).isEqualTo("/test");
        assertThat(menuDO.getComponent()).isEqualTo("TestPage");
        assertThat(menuDO.getMenuType()).isEqualTo("C");
        assertThat(menuDO.getVisible()).isEqualTo("0");
        assertThat(menuDO.getStatus()).isEqualTo("0");
        assertThat(menuDO.getPermission()).isEqualTo("test:view");
        assertThat(menuDO.getIcon()).isEqualTo("el-icon-menu");
        assertThat(menuDO.getRemark()).isEqualTo("菜单备注");
    }

    @Test
    void systemMenuConvert_updateVO2DO_字段全映射() {
        SystemMenuUpdateVO updateVO = new SystemMenuUpdateVO();
        updateVO.setId(5L);
        updateVO.setParentId(1L);
        updateVO.setMenuName("更新菜单");
        updateVO.setSort(10);
        updateVO.setPath("/updated");
        updateVO.setComponent("UpdatedPage");
        updateVO.setMenuType("M");
        updateVO.setVisible("1");
        updateVO.setStatus("1");
        updateVO.setPermission("test:edit");
        updateVO.setIcon("el-icon-edit");
        updateVO.setRemark("更新备注");

        SystemMenuDO menuDO = SystemMenuConvert.INSTANCE.updateVO2DO(updateVO);

        assertThat(menuDO.getId()).isEqualTo(5L);
        assertThat(menuDO.getMenuName()).isEqualTo("更新菜单");
        assertThat(menuDO.getParentId()).isEqualTo(1L);
        assertThat(menuDO.getSort()).isEqualTo(10);
        assertThat(menuDO.getPath()).isEqualTo("/updated");
        assertThat(menuDO.getComponent()).isEqualTo("UpdatedPage");
        assertThat(menuDO.getMenuType()).isEqualTo("M");
        assertThat(menuDO.getVisible()).isEqualTo("1");
        assertThat(menuDO.getStatus()).isEqualTo("1");
        assertThat(menuDO.getPermission()).isEqualTo("test:edit");
        assertThat(menuDO.getIcon()).isEqualTo("el-icon-edit");
        assertThat(menuDO.getRemark()).isEqualTo("更新备注");
    }

    // ==================== SystemDeptConvert ====================

    @Test
    void systemDeptConvert_addVO2DO_字段全映射() {
        SystemDeptAddVO addVO = new SystemDeptAddVO();
        addVO.setParentId(0L);
        addVO.setDeptCode("TECH");
        addVO.setDeptName("技术部");
        addVO.setLeaderId(100L);
        addVO.setSort(1);
        addVO.setStatus("0");
        addVO.setRemark("技术部门");

        SystemDeptDO deptDO = SystemDeptConvert.INSTANCE.addVO2DO(addVO);

        assertThat(deptDO.getParentId()).isEqualTo(0L);
        assertThat(deptDO.getDeptCode()).isEqualTo("TECH");
        assertThat(deptDO.getDeptName()).isEqualTo("技术部");
        assertThat(deptDO.getLeaderId()).isEqualTo(100L);
        assertThat(deptDO.getSort()).isEqualTo(1);
        assertThat(deptDO.getStatus()).isEqualTo("0");
        assertThat(deptDO.getRemark()).isEqualTo("技术部门");
    }

    @Test
    void systemDeptConvert_updateVO2DO_字段全映射() {
        SystemDeptUpdateVO updateVO = new SystemDeptUpdateVO();
        updateVO.setId(3L);
        updateVO.setParentId(1L);
        updateVO.setDeptCode("RD");
        updateVO.setDeptName("研发部");
        updateVO.setLeaderId(200L);
        updateVO.setSort(2);
        updateVO.setStatus("1");
        updateVO.setRemark("研发部门");

        SystemDeptDO deptDO = SystemDeptConvert.INSTANCE.updateVO2DO(updateVO);

        assertThat(deptDO.getId()).isEqualTo(3L);
        assertThat(deptDO.getParentId()).isEqualTo(1L);
        assertThat(deptDO.getDeptCode()).isEqualTo("RD");
        assertThat(deptDO.getDeptName()).isEqualTo("研发部");
        assertThat(deptDO.getLeaderId()).isEqualTo(200L);
        assertThat(deptDO.getSort()).isEqualTo(2);
        assertThat(deptDO.getStatus()).isEqualTo("1");
        assertThat(deptDO.getRemark()).isEqualTo("研发部门");
    }

    @Test
    void systemDeptConvert_do2TreeVO_字段全映射() {
        SystemDeptDO deptDO = new SystemDeptDO();
        deptDO.setId(1L);
        deptDO.setParentId(0L);
        deptDO.setDeptCode("ROOT");
        deptDO.setDeptName("总公司");
        deptDO.setLeaderId(99L);
        deptDO.setSort(0);
        deptDO.setStatus("0");

        SystemDeptTreeVO treeVO = SystemDeptConvert.INSTANCE.do2TreeVO(deptDO);

        assertThat(treeVO.getId()).isEqualTo(1L);
        assertThat(treeVO.getParentId()).isEqualTo(0L);
        assertThat(treeVO.getDeptCode()).isEqualTo("ROOT");
        assertThat(treeVO.getDeptName()).isEqualTo("总公司");
        assertThat(treeVO.getLeaderId()).isEqualTo(99L);
        assertThat(treeVO.getSort()).isEqualTo(0);
        assertThat(treeVO.getStatus()).isEqualTo("0");
    }

    // ==================== SystemDictDataConvert ====================

    @Test
    void systemDictDataConvert_addVO2DO_字段全映射() {
        SystemDictDataAddVO addVO = new SystemDictDataAddVO();
        addVO.setDictType("sys_user_sex");
        addVO.setLabel("男");
        addVO.setValue("1");
        addVO.setCssClass("default");
        addVO.setListClass("primary");
        addVO.setSort("1");
        addVO.setStatus("0");
        addVO.setRemark("性别男");

        SystemDictDataDO dictDataDO = SystemDictDataConvert.INSTANCE.addVO2DO(addVO);

        assertThat(dictDataDO.getDictType()).isEqualTo("sys_user_sex");
        assertThat(dictDataDO.getLabel()).isEqualTo("男");
        assertThat(dictDataDO.getValue()).isEqualTo("1");
        assertThat(dictDataDO.getCssClass()).isEqualTo("default");
        assertThat(dictDataDO.getListClass()).isEqualTo("primary");
        assertThat(dictDataDO.getSort()).isEqualTo(1);
        assertThat(dictDataDO.getStatus()).isEqualTo("0");
        assertThat(dictDataDO.getRemark()).isEqualTo("性别男");
    }

    // ==================== SystemDictTypeConvert ====================

    @Test
    void systemDictTypeConvert_addVO2DO_字段全映射() {
        SystemDictTypeAddVO addVO = new SystemDictTypeAddVO();
        addVO.setDictName("用户性别");
        addVO.setDictType("sys_user_sex");
        addVO.setRemark("用户性别字典");

        SystemDictTypeDO dictTypeDO = SystemDictTypeConvert.INSTANCE.addVO2DO(addVO);

        assertThat(dictTypeDO.getDictName()).isEqualTo("用户性别");
        assertThat(dictTypeDO.getDictType()).isEqualTo("sys_user_sex");
        assertThat(dictTypeDO.getRemark()).isEqualTo("用户性别字典");
    }

    // ==================== SystemPostConvert ====================

    @Test
    void systemPostConvert_addVO2DO_字段全映射() {
        SystemPostAddVO addVO = new SystemPostAddVO();
        addVO.setPostCode("dev");
        addVO.setPostName("开发工程师");
        addVO.setStatus("0");
        addVO.setRemark("开发岗");

        SystemPostDO postDO = SystemPostConvert.INSTANCE.addVO2DO(addVO);

        assertThat(postDO.getPostCode()).isEqualTo("dev");
        assertThat(postDO.getPostName()).isEqualTo("开发工程师");
        assertThat(postDO.getStatus()).isEqualTo("0");
        assertThat(postDO.getRemark()).isEqualTo("开发岗");
    }

    // ==================== SystemRoleConvert ====================

    @Test
    void systemRoleConvert_addVO2DO_字段全映射() {
        SystemRoleAddVO addVO = new SystemRoleAddVO();
        addVO.setRoleCode("user");
        addVO.setRoleName("普通用户");
        addVO.setDataScope("2");
        addVO.setStatus("0");
        addVO.setRemark("普通用户角色");

        SystemRoleDO roleDO = SystemRoleConvert.INSTANCE.addVO2DO(addVO);

        assertThat(roleDO.getRoleCode()).isEqualTo("user");
        assertThat(roleDO.getRoleName()).isEqualTo("普通用户");
        assertThat(roleDO.getDataScope()).isEqualTo("2");
        assertThat(roleDO.getStatus()).isEqualTo("0");
        assertThat(roleDO.getRemark()).isEqualTo("普通用户角色");
    }

    // ==================== SystemConfigConvert ====================

    @Test
    void systemConfigConvert_addVO2DO_字段全映射() {
        SystemConfigAddVO addVO = new SystemConfigAddVO();
        addVO.setConfigName("系统名称");
        addVO.setConfigKey("sys.site.name");
        addVO.setConfigValue("XD11CC");
        addVO.setRemark("站点名称配置");

        SystemConfigDO configDO = SystemConfigConvert.INSTANCE.addVO2DO(addVO);

        assertThat(configDO.getConfigName()).isEqualTo("系统名称");
        assertThat(configDO.getConfigKey()).isEqualTo("sys.site.name");
        assertThat(configDO.getConfigValue()).isEqualTo("XD11CC");
        assertThat(configDO.getRemark()).isEqualTo("站点名称配置");
    }

    // ==================== SystemTenantConvert ====================

    @Test
    void systemTenantConvert_addVO2DO_字段全映射() {
        SystemTenantAddVO addVO = new SystemTenantAddVO();
        addVO.setName("租户A");
        addVO.setContactName("联系人A");
        addVO.setContactPhone("74955953457");
        addVO.setAccountCount(10);
        addVO.setStatus("0");

        SystemTenantDO tenantDO = SystemTenantConvert.INSTANCE.addVO2DO(addVO);

        assertThat(tenantDO.getName()).isEqualTo("租户A");
        assertThat(tenantDO.getContactName()).isEqualTo("联系人A");
        assertThat(tenantDO.getContactPhone()).isEqualTo("74955953457");
        assertThat(tenantDO.getAccountCount()).isEqualTo(10);
        assertThat(tenantDO.getStatus()).isEqualTo("0");
    }

    // ==================== SystemNoticeConvert ====================

    @Test
    void systemNoticeConvert_addVO2DO_字段全映射() {
        SystemNoticeAddVO addVO = new SystemNoticeAddVO();
        addVO.setTitle("系统通知");
        addVO.setContent("这是一条通知内容");
        addVO.setType(1);
        addVO.setScope(2);
        addVO.setRemark("通知备注");

        SystemNoticeDO noticeDO = SystemNoticeConvert.INSTANCE.addVO2DO(addVO);

        assertThat(noticeDO.getTitle()).isEqualTo("系统通知");
        assertThat(noticeDO.getContent()).isEqualTo("这是一条通知内容");
        assertThat(noticeDO.getType()).isEqualTo(1);
        assertThat(noticeDO.getScope()).isEqualTo(2);
        assertThat(noticeDO.getRemark()).isEqualTo("通知备注");
    }

    // ==================== AuthClientConfigConvert ====================

    @Test
    void authClientConfigConvert_addVO2DO_字段全映射() {
        AuthClientConfigAddVO addVO = new AuthClientConfigAddVO();
        addVO.setSource("LOCAL");
        addVO.setClientId("client1");
        addVO.setClientSecret("secret1");
        addVO.setRedirectUri("https://example.com/callback");
        addVO.setName("测试客户端");
        addVO.setIcon("el-icon-user");
        addVO.setSort(1);
        addVO.setStatus("0");
        addVO.setRemark("OAuth2客户端");

        AuthClientConfigDO clientDO = AuthClientConfigConvert.INSTANCE.addVO2DO(addVO);

        assertThat(clientDO.getSource()).isEqualTo("LOCAL");
        assertThat(clientDO.getClientId()).isEqualTo("client1");
        assertThat(clientDO.getClientSecret()).isEqualTo("secret1");
        assertThat(clientDO.getRedirectUri()).isEqualTo("https://example.com/callback");
        assertThat(clientDO.getName()).isEqualTo("测试客户端");
        assertThat(clientDO.getIcon()).isEqualTo("el-icon-user");
        assertThat(clientDO.getSort()).isEqualTo(1);
        assertThat(clientDO.getStatus()).isEqualTo("0");
        assertThat(clientDO.getRemark()).isEqualTo("OAuth2客户端");
    }

    // ==================== 共享辅助方法 ====================

    private SystemUserDO buildUserDO() {
        SystemUserDO userDO = new SystemUserDO();
        userDO.setId(88L);
        userDO.setUsername("testuser");
        userDO.setPassword("encrypted");
        userDO.setNickname("测试用户");
        userDO.setIdCard("110101199001011234");
        userDO.setSex("1");
        userDO.setPhone("74955953457");
        userDO.setEmail("test@xd11cc.com");
        userDO.setDeptId(10L);
        userDO.setDeptName("技术部");
        userDO.setPostId(1L);
        userDO.setPostName("开发工程师");
        userDO.setStatus("0");
        userDO.setHeadUrl("/avatar.png");
        userDO.setRemark("备注信息");
        userDO.setTenantId(10L);
        return userDO;
    }

    private void assertCommonUserFields(UserLoginInfoVO vo) {
        assertThat(vo.getId()).isEqualTo(88L);
        assertThat(vo.getUsername()).isEqualTo("testuser");
        assertThat(vo.getNickname()).isEqualTo("测试用户");
        assertThat(vo.getIdCard()).isEqualTo("110101199001011234");
        assertThat(vo.getSex()).isEqualTo("1");
        assertThat(vo.getPhone()).isEqualTo("74955953457");
        assertThat(vo.getEmail()).isEqualTo("test@xd11cc.com");
        assertThat(vo.getHeadUrl()).isEqualTo("/avatar.png");
        assertThat(vo.getDeptId()).isEqualTo(10L);
        assertThat(vo.getDeptName()).isEqualTo("技术部");
        assertThat(vo.getPostId()).isEqualTo(1L);
        assertThat(vo.getPostName()).isEqualTo("开发工程师");
    }
}
