package com.zt.utils;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zt.entity.RedisCacheData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存穿透   缓存击穿  工具
 * @author ZT
 */
@Component
@Slf4j
public class CacheClient {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    /**
     * 互斥锁 解决 缓存击穿
     * @param keyPrefix 缓存key前缀
     * @param id 业务id
     * @param type 业务bean type
     * @param lockPrefix 互斥锁key前缀
     * @param func 获取数据库数据method
     * @param expireTime 缓存过期时间
     * @param <ID> 业务编号泛型
     * @param <R> 业务数据泛型
     * @return R
     */
    public  <ID, R> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, String lockPrefix, Function<ID, R> func, long expireTime) {
        String cacheKey = keyPrefix + id;
        // 1.获取缓存数据
        String data = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(data)) {
            return JSONUtil.toBean(data, type);
        }
        // 2.缓存未命中则使用互斥锁 查询数据
        String lockKey = lockPrefix + id;
        R r = null;
        try {
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                log.info("lock fail, has try get data...");
                ThreadUtil.sleep(50);
                return queryWithMutex(keyPrefix, id, type, lockPrefix, func, expireTime);
            }
            // 模拟重建缓存延迟
            ThreadUtil.sleep(500);
            r = func.apply(id);
            if(Objects.isNull(r)) {
                // 缓存空对象
                stringRedisTemplate.opsForValue().set(cacheKey, "", 300, TimeUnit.SECONDS);
                return null;
            }
            stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(r), expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("error = {}", e);
        } finally {
            releaseLock(lockKey);
        }
        return r;
    }


    private boolean tryLock(String key) {
        Boolean ifAbsent = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 30L, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(ifAbsent);
    }


    private void releaseLock(String key) {
        stringRedisTemplate.delete(key);
    }


    /**
     * 使用逻辑过期时间 解决缓存击穿
     * @param keyPrefix 缓存key前缀
     * @param id 业务id
     * @param type 业务bean type
     * @param lockPrefix 互斥锁key前缀
     * @param func 获取数据库数据method
     * @param expireTime 缓存过期时间
     * @param <ID> 业务编号泛型
     * @param <R> 业务数据泛型
     * @return R
     */
    public  <ID, R> R queryWithLogicExpireTime(String keyPrefix, ID id, Class<R> type, String lockPrefix, Function<ID, R> func, long expireTime) {
        String cacheData = stringRedisTemplate.opsForValue().get(keyPrefix + id);
        if (StrUtil.isBlank(cacheData)) {
            // 无数据就不存在
            return null;
        }
        RedisCacheData redisCacheData = JSONUtil.toBean(cacheData, RedisCacheData.class);
        LocalDateTime curExpireTime = redisCacheData.getExpireTime();
        R r = JSONUtil.toBean((JSONObject) redisCacheData.getData(), type);
        if (LocalDateTime.now().isBefore(curExpireTime)) {
            // 缓存未过期
            return r;
        }
        // 缓存过期  缓存重建
        String lockKey = lockPrefix + id;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            THREAD_POOL.submit(() -> {
                try {
                    log.info(">>>>>>缓存重建");
                    ThreadUtil.sleep(500);
                    saveData2Redis(id, expireTime, func, keyPrefix);
                } catch (Exception e) {
                    log.error("error = {}", e);
                } finally {
                    releaseLock(lockKey);
                }
            });
        }
        return r;
    }


    /**
     * 设置redis逻辑过期时间
     *
     * @param id
     * @param expireTime 逻辑过期时间
     */
    private <ID, R> void saveData2Redis(ID id, long expireTime, Function<ID, R> func, String keyPrefix) {
        R r = func.apply(id);
        RedisCacheData<R> redisCacheData = RedisCacheData.<R>builder()
                .data(r)
                .expireTime(LocalDateTime.now().plusSeconds(expireTime))
                .build();
        stringRedisTemplate.opsForValue().set(keyPrefix + id, JSONUtil.toJsonStr(redisCacheData));
    }
}
