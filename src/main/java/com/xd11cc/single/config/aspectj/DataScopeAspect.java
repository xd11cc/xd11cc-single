package com.xd11cc.single.config.aspectj;

import com.xd11cc.single.config.annotation.DataScope;
import com.xd11cc.single.config.exception.ServiceException;
import com.xd11cc.single.entity.base.BaseQueryVO;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.DataScopeEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.utils.SecurityUtils;
import com.xd11cc.single.utils.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xd11cc
 * @date 2026-05-28
 * @description todo 初步实现数据隔离，具体情况需结合业务
 */
@Aspect
@Component
public class DataScopeAspect {

    @Before("@annotation(com.xd11cc.single.config.annotation.DataScope)")
    public void doBefore(JoinPoint joinPoint) {
        LoginUserDTO loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new ServiceException(SystemErrorEnum.UNAUTHORIZED);
        }
        String userDataScope = loginUser.getDataScope();
        // 未设置数据权限，拥有全部数据权限，超级管理员
        if (StringUtils.isEmpty(userDataScope) ||
                DataScopeEnum.ALL.getCode().equals(userDataScope) ||
                SystemUserDO.isAdmin(loginUser.getUserId())) {
            return;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DataScope dataScope = method.getAnnotation(DataScope.class);

        BaseQueryVO queryVO = findQueryVO(joinPoint.getArgs());
        if (queryVO == null) {
            return;
        }

        String sqlFilter = buildSqlFilter(loginUser, dataScope);
        queryVO.setDataScope(sqlFilter);
    }

    private String buildSqlFilter(LoginUserDTO loginUser, DataScope dataScope) {
        String userDataScope = loginUser.getDataScope();
        String deptCol = resolveColumn(dataScope.alias(), dataScope.deptColumn());
        String userCol = resolveColumn(dataScope.alias(), dataScope.userColumn());

        if (DataScopeEnum.SELF.getCode().equals(userDataScope)) {
            return userCol + " = " + loginUser.getUserId();
        }

        Set<Long> deptIds = loginUser.getDataScopeDeptIds();
        if (deptIds == null || deptIds.isEmpty()) {
            return userCol + " = " + loginUser.getUserId();
        }

        String ids = deptIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return deptCol + " IN (" + ids + ")";
    }

    private String resolveColumn(String alias, String column) {
        if (StringUtils.isNotEmpty(alias)) {
            return alias + "." + column;
        }
        return column;
    }

    private BaseQueryVO findQueryVO(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof BaseQueryVO) {
                return (BaseQueryVO) arg;
            }
        }
        return null;
    }
}
