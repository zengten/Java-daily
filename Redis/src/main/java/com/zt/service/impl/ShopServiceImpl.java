package com.zt.service.impl;


import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.Result;
import com.zt.entity.RedisCacheData;
import com.zt.entity.Shop;
import com.zt.mapper.ShopMapper;
import com.zt.service.IShopService;
import com.zt.utils.CacheClient;
import com.zt.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;

    /**
     * 直接使用缓存注解  进行缓存
     * 缓存穿透：
     *   1.缓存空对象 （简单）
     *   2.布隆过滤器 （复杂代码逻辑）
     */
//    @Cacheable(key = "#id")
//    @Override
//    public Result queryShopById(Long id) {
//        return Result.ok(getById(id));
//    }


    /**
     * 缓存击穿：
     * 1.使用互斥锁  ->  业务接口查询可能过长
     * 2.数据逻辑过期  ->  额外的缓存空间数据
     */
    @Override
    @SuppressWarnings("all")
    public Result queryShopById(Long id) {
//        Shop shop = queryWithMutex(id);

//        Shop shop = queryWithLogicExpireTime(id);

//        Shop shop = cacheClient.queryWithMutex(RedisConstants.CACHE_SHOP_KEY, id, Shop.class,
//                RedisConstants.LOCK_SHOP_KEY, this::getById, 3600 + RandomUtil.randomInt(1000));

        Shop shop = cacheClient.queryWithLogicExpireTime(RedisConstants.CACHE_SHOP_KEY, id, Shop.class,
                RedisConstants.LOCK_SHOP_KEY, this::getById, 3600 + RandomUtil.randomInt(1000));
        return Result.ok(shop);
    }

    /**
     * 使用逻辑过期时间
     */
    private Shop queryWithLogicExpireTime(Long id) {
        String cacheData = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        if (StrUtil.isBlank(cacheData)) {
            // 无数据就不存在
            return null;
        }
        RedisCacheData redisCacheData = JSONUtil.toBean(cacheData, RedisCacheData.class);
        LocalDateTime expireTime = redisCacheData.getExpireTime();
        Shop shop = JSONUtil.toBean((JSONObject) redisCacheData.getData(), Shop.class);
        if (LocalDateTime.now().isBefore(expireTime)) {
            // 缓存未过期
            return shop;
        }
        // 缓存过期  缓存重建
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            THREAD_POOL.submit(() -> {
                try {
                    log.info(">>>>>>缓存重建");
                    ThreadUtil.sleep(500);
                    saveData2Redis(id, 1800);
                } catch (Exception e) {
                    log.error("error = {}", e);
                } finally {
                    releaseLock(lockKey);
                }
            });
        }
        return shop;
    }


    /**
     * 设置redis逻辑过期时间
     *
     * @param id
     * @param expireTime 逻辑过期时间
     */
    public void saveData2Redis(Long id, long expireTime) {
        Shop shop = getById(id);
        RedisCacheData<Shop> redisCacheData = RedisCacheData.<Shop>builder()
                .data(shop)
                .expireTime(LocalDateTime.now().plusSeconds(expireTime))
                .build();
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisCacheData));
    }


    /**
     * 使用互斥锁
     */
    private Shop queryWithMutex(Long id) {
        String cacheKey = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.获取缓存数据
        String cacheData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cacheData)) {
            return JSONUtil.toBean(cacheData, Shop.class);
        }
        // 2.缓存未命中则使用互斥锁 查询数据
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                log.info("lock fail, has try get data...");
                ThreadUtil.sleep(50);
                return queryWithMutex(id);
            }
            // 模拟重建缓存延迟
            ThreadUtil.sleep(500);
            shop = getById(id);
            // 缓存空对象
            stringRedisTemplate.opsForValue().set(cacheKey,
                    Objects.isNull(shop) ? "" : JSONUtil.toJsonStr(shop), 1800, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("error = {}", e);
        } finally {
            releaseLock(lockKey);
        }
        return shop;
    }

    private boolean tryLock(String key) {
        Boolean ifAbsent = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 30, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(ifAbsent);
    }


    private void releaseLock(String key) {
        stringRedisTemplate.delete(key);
    }


    /**
     * 缓存更新策略：
     * 1.先清除缓存，再更新数据
     * 2.先更新数据，再清除缓存
     * 一般选择第二种方式   缓存一致性问题较低概率
     */
    @CacheEvict(key = "#shop.id")
    @Override
    public Result updateShopById(Shop shop) {
        boolean b = updateById(shop);
        return Result.ok();
    }
}
