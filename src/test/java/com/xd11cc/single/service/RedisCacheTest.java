package com.xd11cc.single.service;

import com.xd11cc.single.config.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: xd11cc
 * @Date: 2025/6/17 18:01
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisCacheTest {

    @Autowired
    private RedisCache redisCache;

    @Test
    public void setRedisCache(){
        redisCache.setCacheObject("key","value");
    }

    @Test
    public void getRedisCache(){
        log.info(redisCache.getCacheObject("key"));
    }

    @Test
    public void delRedisCache(){
        redisCache.removeCacheObject("key");
    }
}
