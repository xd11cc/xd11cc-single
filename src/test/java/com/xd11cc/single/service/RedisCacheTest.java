package com.xd11cc.single.service;

import cn.hutool.core.date.DateUtil;
import com.xd11cc.single.config.RedisCache;
import com.xd11cc.single.constants.CacheConstants;
import com.xd11cc.single.entity.dto.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @Test
    public void setCacheMapObject(){
        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setId(1L);
        tenantDTO.setName("xd11cc租户");
        tenantDTO.setStatus("0");
        tenantDTO.setDomain("xd11cc.xyz");
        tenantDTO.setExpireTime(DateUtil.offsetDay(new Date(), 1000));
        tenantDTO.setAccountCount(100);
        Map<String,Object> map = new HashMap<>();
        map.put("localhost", tenantDTO);
        map.put("127.0.0.1", tenantDTO);
        map.forEach((k,v)->{
            redisCache.setCacheMapValue(CacheConstants.TENANT_DOMAIN_KEY, k, v, false);
        });
    }
}
