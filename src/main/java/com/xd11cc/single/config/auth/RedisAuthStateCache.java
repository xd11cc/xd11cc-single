package com.xd11cc.single.config.auth;

import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author xd11cc
 * @date 2026-04-29 15:18:58
 * @description 重写state缓存方式
 */
@Component
public class RedisAuthStateCache implements AuthStateCache {

    @Autowired
    private RedisCache redisCache;

    private String getAuthStateKey(String key){
        return CacheConstants.AUTH_STATE_KEY + key;
    }

    @Override
    public void cache(String key, String value) {
        redisCache.setCacheObject(getAuthStateKey(key), value, AuthCacheConfig.timeout, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param key
     * @param value
     * @param timeout 毫秒
     */
    @Override
    public void cache(String key, String value, long timeout) {
        redisCache.setCacheObject(getAuthStateKey(key), value, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public String get(String key) {
        return redisCache.getCacheObject(getAuthStateKey(key));
    }

    @Override
    public boolean containsKey(String key) {
        return redisCache.hasKey(getAuthStateKey(key));
    }
}
