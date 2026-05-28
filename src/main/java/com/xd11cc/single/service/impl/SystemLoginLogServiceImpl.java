package com.xd11cc.single.service.impl;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xd11cc.single.config.context.TenantContextHolder;
import com.xd11cc.single.entity.domain.SystemLoginLogDO;
import com.xd11cc.single.entity.vo.SystemLoginLogQueryVO;
import com.xd11cc.single.enums.OperateStatusEnum;
import com.xd11cc.single.enums.LoginTypeEnum;
import com.xd11cc.single.mapper.SystemLoginLogMapper;
import com.xd11cc.single.service.ISystemLoginLogService;
import com.xd11cc.single.utils.IpUtils;
import com.xd11cc.single.utils.ServletUtils;
import com.xd11cc.single.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author xd11cc
 * @date 2026-05-28
 */
@Slf4j
@Service
public class SystemLoginLogServiceImpl extends ServiceImpl<SystemLoginLogMapper, SystemLoginLogDO> implements ISystemLoginLogService {

    @Autowired
    @Qualifier("operateLogExecutor")
    private Executor operateLogExecutor;

    @Override
    public void recordLoginLog(String username, LoginTypeEnum loginType, OperateStatusEnum status, String msg) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            String ip = IpUtils.getIpAddr(request);
            String userAgentStr = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
            Long tenantId = TenantContextHolder.getTenantId();

            SystemLoginLogDO logDO = new SystemLoginLogDO();
            logDO.setUsername(username);
            logDO.setLoginType(loginType.getCode());
            logDO.setStatus(status.getCode());
            logDO.setLoginIp(ip);
            logDO.setBrowser(userAgent != null ? userAgent.getBrowser().getName() : null);
            logDO.setOs(userAgent != null ? userAgent.getOs().getName() : null);
            logDO.setMsg(msg);
            logDO.setLoginTime(new Date());
            logDO.setTenantId(tenantId);

            operateLogExecutor.execute(() -> {
                try {
                    baseMapper.insert(logDO);
                } catch (Exception e) {
                    log.error("保存登录日志失败", e);
                }
            });
        } catch (Exception e) {
            log.error("记录登录日志异常", e);
        }
    }

    @Override
    public List<SystemLoginLogDO> getList(SystemLoginLogQueryVO queryVO) {
        LambdaQueryWrapper<SystemLoginLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryVO.getUsername()),
                SystemLoginLogDO::getUsername, queryVO.getUsername());
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getLoginType()),
                SystemLoginLogDO::getLoginType, queryVO.getLoginType());
        wrapper.eq(StringUtils.isNotEmpty(queryVO.getStatus()),
                SystemLoginLogDO::getStatus, queryVO.getStatus());
        wrapper.ge(queryVO.getBeginTime() != null,
                SystemLoginLogDO::getLoginTime, queryVO.getBeginTime());
        wrapper.le(queryVO.getEndTime() != null,
                SystemLoginLogDO::getLoginTime, queryVO.getEndTime());
        wrapper.orderByDesc(SystemLoginLogDO::getId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return baseMapper.deleteBatchIds(ids);
    }

    @Override
    public void clean() {
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}
