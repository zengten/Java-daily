package com.zt.controller;

import com.zt.annotation.NoLogin;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 测试redis读写分离
 */
@RestController
public class RedisTestController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/get/{key}")
    @NoLogin
    public String hi(@PathVariable String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @GetMapping("/set/{key}/{value}")
    @NoLogin
    public String hi(@PathVariable String key, @PathVariable String value) {
        stringRedisTemplate.opsForValue().set(key, value);
        return "success";
    }
}