package com.xd11cc.single.config.aspectj;

import com.xd11cc.single.config.annotation.DataScope;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.base.BaseQueryVO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.DataScopeEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.utils.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataScopeAspectTest {

    private final DataScopeAspect aspect = new DataScopeAspect();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== 未登录用户 ====================

    @Test
    void doBefore_未登录用户_抛UNAUTHORIZED() {
        JoinPoint joinPoint = mockJoinPoint(NoDataScopeService.class, null, new Object[]{});

        assertThatThrownBy(() -> aspect.doBefore(joinPoint))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getErrorCode().getErrorCode()).isEqualTo(401));
    }

    // ==================== 管理员或全部权限 ====================

    @Test
    void doBefore_空数据权限_不设置过滤() {
        JoinPoint joinPoint = mockJoinPoint(ScopedService.class, ScopedService.METHOD, new Object[]{new ScopedQueryVO()});
        mockLoginUser(1L, "admin", 10L, DataScopeEnum.CUSTOM.getCode(), Collections.emptySet());

        aspect.doBefore(joinPoint);

        assertThat(getQueryVOFrom(joinPoint).getDataScope()).isNull();
    }

    @Test
    void doBefore_ALL数据权限_不设置过滤() {
        JoinPoint joinPoint = mockJoinPoint(ScopedService.class, ScopedService.METHOD, new Object[]{new ScopedQueryVO()});
        mockLoginUser(2L, "user", 10L, DataScopeEnum.ALL.getCode(), Collections.emptySet());

        aspect.doBefore(joinPoint);

        assertThat(getQueryVOFrom(joinPoint).getDataScope()).isNull();
    }

    @Test
    void doBefore_超级管理员_不设置过滤() {
        JoinPoint joinPoint = mockJoinPoint(ScopedService.class, ScopedService.METHOD, new Object[]{new ScopedQueryVO()});
        mockLoginUser(1L, "superadmin", 10L, DataScopeEnum.SELF.getCode(), Collections.singleton(1L));

        aspect.doBefore(joinPoint);

        assertThat(getQueryVOFrom(joinPoint).getDataScope()).isNull();
    }

    // ==================== SELF 模式 ====================

    @Test
    void doBefore_SELF模式_设置用户列过滤() {
        JoinPoint joinPoint = mockJoinPoint(ScopedService.class, ScopedService.METHOD, new Object[]{new ScopedQueryVO()});
        mockLoginUser(5L, "user5", 10L, DataScopeEnum.SELF.getCode(), Collections.singleton(5L));

        aspect.doBefore(joinPoint);

        BaseQueryVO queryVO = getQueryVOFrom(joinPoint);
        assertThat(queryVO.getDataScope()).isEqualTo("create_user_id = 5");
    }

    // ==================== DEPT 模式 ====================

    @Test
    void doBefore_DEPT模式_设置部门过滤() {
        JoinPoint joinPoint = mockJoinPoint(ScopedService.class, ScopedService.METHOD, new Object[]{new ScopedQueryVO()});
        mockLoginUser(3L, "user3", 10L, DataScopeEnum.DEPT.getCode(), new java.util.LinkedHashSet<>(Arrays.asList(1L, 2L)));

        aspect.doBefore(joinPoint);

        BaseQueryVO queryVO = getQueryVOFrom(joinPoint);
        assertThat(queryVO.getDataScope()).isEqualTo("dept_id IN (1,2)");
    }

    @Test
    void doBefore_DEPT模式部门为空_降级为用户列过滤() {
        JoinPoint joinPoint = mockJoinPoint(ScopedService.class, ScopedService.METHOD, new Object[]{new ScopedQueryVO()});
        mockLoginUser(3L, "user3", 10L, DataScopeEnum.DEPT.getCode(), Collections.emptySet());

        aspect.doBefore(joinPoint);

        BaseQueryVO queryVO = getQueryVOFrom(joinPoint);
        assertThat(queryVO.getDataScope()).isEqualTo("create_user_id = 3");
    }

    // ==================== 无 BaseQueryVO 参数 ====================

    @Test
    void doBefore_无BaseQueryVO参数_抛异常() {
        JoinPoint joinPoint = mockJoinPoint(NoQueryService.class, NoQueryService.METHOD, new Object[]{"not-a-queryvo"});
        mockLoginUser(5L, "user5", 10L, DataScopeEnum.SELF.getCode(), Collections.singleton(5L));

        assertThatThrownBy(() -> aspect.doBefore(joinPoint))
                .isInstanceOf(ServiceException.class);
    }

    // ==================== 带别名 ====================

    @Test
    void doBefore_带别名_拼接带表名前缀() {
        JoinPoint joinPoint = mockJoinPoint(AliasedService.class, AliasedService.METHOD, new Object[]{new ScopedQueryVO()});
        mockLoginUser(5L, "user5", 10L, DataScopeEnum.SELF.getCode(), Collections.singleton(5L));

        aspect.doBefore(joinPoint);

        BaseQueryVO queryVO = getQueryVOFrom(joinPoint);
        assertThat(queryVO.getDataScope()).isEqualTo("alias.create_user_id = 5");
    }

    // ==================== 辅助方法 ====================

    private void mockLoginUser(Long userId, String username, Long tenantId,
                               String dataScope, Set<Long> deptIds) {
        SystemUserDO user = new SystemUserDO();
        user.setId(userId);
        user.setUsername(username);
        user.setTenantId(tenantId);

        LoginUserDTO loginUser = new LoginUserDTO();
        loginUser.setUserId(userId);
        loginUser.setSystemUserDO(user);
        loginUser.setDataScope(dataScope);
        loginUser.setDataScopeDeptIds(deptIds);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private BaseQueryVO getQueryVOFrom(JoinPoint joinPoint) {
        return (BaseQueryVO) joinPoint.getArgs()[0];
    }

    // ==================== 测试用服务类 ====================

    static class NoDataScopeService {
        public void someMethod() {}
    }

    static class ScopedService {
        public static final Method METHOD;
        static {
            try { METHOD = ScopedService.class.getMethod("query", ScopedQueryVO.class); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @DataScope
        public void query(ScopedQueryVO vo) {}
    }

    static class NoQueryService {
        public static final Method METHOD;
        static {
            try { METHOD = NoQueryService.class.getMethod("noQuery", String.class); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @DataScope
        public void noQuery(String name) {}
    }

    static class AliasedService {
        public static final Method METHOD;
        static {
            try { METHOD = AliasedService.class.getMethod("queryAlias", ScopedQueryVO.class); } catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
        @DataScope(alias = "alias", deptColumn = "dept_id", userColumn = "create_user_id")
        public void queryAlias(ScopedQueryVO vo) {}
    }

    @SuppressWarnings("unused")
    static class ScopedQueryVO extends BaseQueryVO {
    }

    // ==================== JoinPoint mock 构建 ====================

    private JoinPoint mockJoinPoint(Class<?> targetClass, Method method, Object[] args) {
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getDeclaringTypeName()).thenReturn(targetClass.getName());
        when(signature.getName()).thenReturn(method != null ? method.getName() : "unknown");

        Object targetInstance;
        try {
            targetInstance = targetClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(targetInstance);
        when(joinPoint.getArgs()).thenReturn(args != null ? args : new Object[]{});
        return joinPoint;
    }
}
