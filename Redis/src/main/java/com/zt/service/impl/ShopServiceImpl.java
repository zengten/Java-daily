package com.zt.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.Result;
import com.zt.entity.RedisCacheData;
import com.zt.entity.Shop;
import com.zt.mapper.ShopMapper;
import com.zt.service.IShopService;
import com.zt.utils.CacheClient;
import com.zt.utils.RedisConstants;
import com.zt.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 根据类型分页查询
        if(x == null || y == null) {
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        // start滚动分页起始位置, end滚动分页结束位置
        int start = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        String key = RedisConstants.SHOP_GEO_KEY + typeId;
        // 根据地理位置查询
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                key,// geo key
                GeoReference.fromCoordinate(x, y),// 当前位置经纬度
                new Distance(5000),// 距离当前位置多远范围搜索 5km
                RedisGeoCommands.GeoSearchCommandArgs
                        .newGeoSearchArgs() // args
                        .includeDistance() // 包含距离
                        .limit(end)// 限制条数
        );
        if(CollectionUtil.isEmpty(results)) {
            return Result.ok(ListUtil.empty());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        // 滚动分页起始位置 大于 数据总量
        if(list.size() <= start) {
            return Result.ok(ListUtil.empty());
        }
        // 内存分页，并保存商铺id和距离
        List<String> shopIdList = new ArrayList<>();
        Map<String, Distance> distanceMap = new HashMap<>();
        list.stream().skip(start).forEach(item -> {
            String shopId = item.getContent().getName();
            shopIdList.add(shopId);
            distanceMap.put(shopId, item.getDistance());
        });
        String lastSql = StrUtil.format(" order by field(id,{})", String.join(",", shopIdList));
        List<Shop> shopList = list(Wrappers.<Shop>lambdaQuery().in(Shop::getId, shopIdList).last(lastSql));
        shopList.forEach(shop -> {
            Distance distance = distanceMap.get(shop.getId().toString());
            shop.setDistance(distance.getValue());
        });
        return Result.ok(shopList);
    }
}
