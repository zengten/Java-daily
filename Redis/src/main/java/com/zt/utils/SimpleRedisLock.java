package com.zt.utils;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.concurrent.TimeUnit;

/**
 * @author ZT
 * @version 1.0
 * @date 2022/10/15 13:43
 */
@Slf4j
public class SimpleRedisLock implements ILock {

    private static final String KEY_PREFIX = "lock:";

    /**
     * 等同标识机器码，防止删除他人的锁
     */
    private static final String VALUE_PREFIX = IdUtil.fastSimpleUUID();

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("/lua/unlock.lua"));
        // 泛型必须为Long
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    private String key;

    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String key, StringRedisTemplate stringRedisTemplate) {
        this.key = key;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(long expireTime) {
        long threadId = Thread.currentThread().getId();
        String value = VALUE_PREFIX + threadId;
        Boolean isLock = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + key, value, expireTime, TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(isLock)) {
            log.info("获取到锁：{}", KEY_PREFIX + key);
        }
        return Boolean.TRUE.equals(isLock);
    }

    @Override
    public void unlock() {
//        unLockTwoStep();
        Long unlock = stringRedisTemplate.execute(UNLOCK_SCRIPT,
                ListUtil.of(KEY_PREFIX + key),
                VALUE_PREFIX + Thread.currentThread().getId());
        if(Long.valueOf(1).equals(unlock)) {
            log.info("释放锁: {}", KEY_PREFIX + key);
        }
    }

    /**
     * 查询再释放锁   不具备原子性
     */
    private void unLockTwoStep() {
        long threadId = Thread.currentThread().getId();
        String value = VALUE_PREFIX + threadId;
        String oldValue = stringRedisTemplate.opsForValue().get(KEY_PREFIX + key);
        if(value.equals(oldValue)) {
            stringRedisTemplate.delete(KEY_PREFIX + key);
            log.info("释放锁: {}", KEY_PREFIX + key);
        }
    }
}
