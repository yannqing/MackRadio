package com.yannqing.mackradio;

import com.yannqing.mackradio.service.UserService;
import com.yannqing.mackradio.utils.RedisCache;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
class RedisTests {

    @Resource
    private UserService userService;

    @Resource
    private RedisCache redisCache;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() throws Exception {
        String cacheObject = redisCache.getCacheObject("test");
        System.out.println();
    }



}
