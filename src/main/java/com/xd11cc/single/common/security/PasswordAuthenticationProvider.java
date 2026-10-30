package com.xd11cc.single.common.security;

import com.xd11cc.single.common.exception.ServiceException;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.constants.UserConstants;
import com.xd11cc.single.entity.domain.SystemUserDO;
import com.xd11cc.single.entity.dto.LoginUserDTO;
import com.xd11cc.single.enums.SystemStatusEnum;
import com.xd11cc.single.enums.SystemErrorEnum;
import com.xd11cc.single.infrastructure.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 密码认证提供者。
 * <p>
 * 把「密码比对」与「错误次数锁定」从 {@code UserDetailsService} 里搬出来。
 * 原先这两件事写在 {@code loadUserByUsername} 中，导致：
 * </p>
 * <ul>
 *     <li>{@code loadUserByUsername} 隐含前置条件「调用前必须把原始密码塞进 SecurityContext」，
 *         而这个条件在方法签名里看不出来</li>
 *     <li>密码被比对两次（手工一次 + {@link DaoAuthenticationProvider} 一次）</li>
 *     <li>锁定策略焊死在「加载用户」的职责上，换任何认证方式都要复制一遍</li>
 * </ul>
 * <p>
 * 现在职责归位：{@code UserDetailsService} 只负责加载用户，
 * 密码校验与失败计数在本类完成，锁定状态通过业务错误码返回。
 * </p>
 *
 * @author xd11cc
 */
@Slf4j
@Component
public class PasswordAuthenticationProvider extends DaoAuthenticationProvider {

    /**
     * 密码错误计数保留时长（分钟）
     */
    private static final int ERROR_COUNT_TTL_MINUTES = 30;

    private final RedisCache redisCache;

    public PasswordAuthenticationProvider(UserDetailsService userDetailsService,
                                          PasswordEncoder passwordEncoder,
                                          RedisCache redisCache) {
        this.redisCache = redisCache;
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        if (!(userDetails instanceof LoginUserDTO)) {
            // 非本项目用户模型，交回父类默认行为
            super.additionalAuthenticationChecks(userDetails, authentication);
            return;
        }

        Long userId = ((LoginUserDTO) userDetails).getUserId();
        String countKey = CacheConstants.PASSWORD_ERROR_COUNT_KEY + userId;

        Integer cached = redisCache.getCacheObject(countKey);
        int current = cached == null ? 0 : cached;
        if (current >= UserConstants.PASSWORD_MAX_RETRY_COUNT) {
            throw new ServiceException(SystemErrorEnum.USER_LOCKED,
                    new Object[]{UserConstants.PASSWORD_MAX_RETRY_COUNT});
        }

        if (authentication.getCredentials() == null) {
            // 保持与改造前一致的语义：缺凭证属于未授权，不计入密码错误次数
            throw new ServiceException(SystemErrorEnum.UNAUTHORIZED);
        }

        try {
            super.additionalAuthenticationChecks(userDetails, authentication);
        } catch (BadCredentialsException e) {
            int next = current + 1;
            redisCache.setCacheObject(countKey, next, ERROR_COUNT_TTL_MINUTES, TimeUnit.MINUTES);
            log.warn("[auth] 密码错误，userId={}，累计 {}/{} 次", userId, next,
                    UserConstants.PASSWORD_MAX_RETRY_COUNT);
            throw new ServiceException(SystemErrorEnum.LOGIN_FAILED);
        }

        // 密码正确之后才检查账户状态：此前放在加载用户阶段，
        // 任何人用错误密码都能探测出「账号存在但已禁用」
        SystemUserDO systemUserDO = ((LoginUserDTO) userDetails).getSystemUserDO();
        if (SystemStatusEnum.FORBIDDEN.getCode().equals(systemUserDO.getStatus())) {
            throw new ServiceException(SystemErrorEnum.USER_FORBIDDEN);
        }

        // 登录成功，清除历史错误计数
        if (redisCache.hasKey(countKey)) {
            redisCache.removeCacheObject(countKey);
        }
    }
}
