package com.zt.utils;

import cn.hutool.core.date.DateUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author ZT
 */
@Component
public class RedisIdGenerator {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdGenerator(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 时间戳开始时间，服务开始开发时间
     */
    private static final long BEGIN_SECOND = 1665713133L;

    /**
     * 生成id方法，redis自增key: keyPrefix + date(yy:MM:dd)
     * 可以通过id统计当天产生多少业务数据
     * @param keyPrefix 生成id前缀
     * @return id
     */
    public long nextId(String keyPrefix) {
        long curTimestamp = DateUtil.currentSeconds() - BEGIN_SECOND;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy:MM:dd");
        String date = LocalDateTime.now().format(formatter);
        Long increment = stringRedisTemplate.opsForValue().increment(keyPrefix + date);
        return curTimestamp << 32 | increment;
    }

    /**
     * def 默认前缀 default
     * @return id
     */
    public long nextId() {
        return nextId("def:");
    }
}
