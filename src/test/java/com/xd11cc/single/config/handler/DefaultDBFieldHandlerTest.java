package com.xd11cc.single.config.handler;

import com.xd11cc.single.entity.base.BaseDO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.utils.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDBFieldHandlerTest {

    private final DefaultDBFieldHandler handler = new DefaultDBFieldHandler();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== insertFill ====================

    @Test
    void insertFill_未登录_设置创建时间更新时间和delFlag() {
        BaseDO baseDO = new BaseDO();
        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.insertFill(metaObject);

        assertThat(baseDO.getCreateTime()).isNotNull();
        assertThat(baseDO.getUpdateTime()).isNotNull();
        assertThat(baseDO.getDelFlag()).isFalse();
        assertThat(baseDO.getCreateUserId()).isNull();
        assertThat(baseDO.getUpdateUserId()).isNull();
    }

    @Test
    void insertFill_已登录_设置创建人和更新人() {
        mockLoginUser(88L);

        BaseDO baseDO = new BaseDO();
        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.insertFill(metaObject);

        assertThat(baseDO.getCreateUserId()).isEqualTo(88L);
        assertThat(baseDO.getUpdateUserId()).isEqualTo(88L);
    }

    @Test
    void insertFill_字段已设置_不覆盖() {
        mockLoginUser(88L);

        Date now = new Date();
        BaseDO baseDO = new BaseDO();
        baseDO.setCreateTime(now);
        baseDO.setUpdateTime(now);
        baseDO.setCreateUserId(1L);
        baseDO.setUpdateUserId(1L);

        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.insertFill(metaObject);

        assertThat(baseDO.getCreateTime()).isEqualTo(now);
        assertThat(baseDO.getUpdateTime()).isEqualTo(now);
        assertThat(baseDO.getCreateUserId()).isEqualTo(1L);
        assertThat(baseDO.getUpdateUserId()).isEqualTo(1L);
    }

    @Test
    void insertFill_部分字段已设置_设置缺失字段() {
        mockLoginUser(88L);

        Date now = new Date();
        BaseDO baseDO = new BaseDO();
        baseDO.setCreateTime(now);

        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.insertFill(metaObject);

        assertThat(baseDO.getCreateTime()).isEqualTo(now);
        assertThat(baseDO.getUpdateTime()).isNotNull();
        assertThat(baseDO.getCreateUserId()).isEqualTo(88L);
        assertThat(baseDO.getUpdateUserId()).isEqualTo(88L);
    }

    @Test
    void insertFill_非BaseDO对象_不做处理() {
        String notBaseDO = "just a string";
        MetaObject metaObject = MetaObject.forObject(notBaseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.insertFill(metaObject);
    }

    // ==================== updateFill ====================

    @Test
    void updateFill_总是更新updateTime() {
        BaseDO baseDO = new BaseDO();
        baseDO.setUpdateTime(new Date(0L)); // 旧值
        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.updateFill(metaObject);

        assertThat(baseDO.getUpdateTime()).isNotNull();
        assertThat(baseDO.getUpdateTime().getTime()).isNotEqualTo(0L);
    }

    @Test
    void updateFill_已登录且updateUserId为空_设置用户() {
        mockLoginUser(99L);

        BaseDO baseDO = new BaseDO();
        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.updateFill(metaObject);

        assertThat(baseDO.getUpdateUserId()).isEqualTo(99L);
    }

    @Test
    void updateFill_已登录但updateUserId已设置_不覆盖() {
        mockLoginUser(99L);

        BaseDO baseDO = new BaseDO();
        baseDO.setUpdateUserId(55L);
        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.updateFill(metaObject);

        assertThat(baseDO.getUpdateUserId()).isEqualTo(55L);
    }

    @Test
    void updateFill_未登录_updateUserId仍为null() {
        BaseDO baseDO = new BaseDO();
        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.updateFill(metaObject);

        assertThat(baseDO.getUpdateTime()).isNotNull();
        assertThat(baseDO.getUpdateUserId()).isNull();
    }

    @Test
    void updateFill_非BaseDO对象_不做处理() {
        String notBaseDO = "just a string";
        MetaObject metaObject = MetaObject.forObject(notBaseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.updateFill(metaObject);
    }

    // ==================== 多租户隔离 ====================

    @Test
    void insertFill_不同租户独立创建人_设置各自创建人() {
        mockLoginUser(10L); // 租户10

        BaseDO baseDO = new BaseDO();
        MetaObject metaObject = MetaObject.forObject(baseDO,
                new org.apache.ibatis.reflection.factory.DefaultObjectFactory(),
                new org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory(),
                new org.apache.ibatis.reflection.DefaultReflectorFactory());

        handler.insertFill(metaObject);

        assertThat(baseDO.getCreateUserId()).isEqualTo(10L);
        assertThat(baseDO.getUpdateUserId()).isEqualTo(10L);
        assertThat(baseDO.getDelFlag()).isFalse();
    }

    // ==================== 辅助方法 ====================

    private void mockLoginUser(Long userId) {
        SystemUserDO user = new SystemUserDO();
        user.setId(userId);

        LoginUserDTO loginUser = new LoginUserDTO();
        loginUser.setUserId(userId);
        loginUser.setSystemUserDO(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
