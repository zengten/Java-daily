package com.zt;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zt.entity.Shop;
import com.zt.service.impl.ShopServiceImpl;
import com.zt.utils.RedisConstants;
import com.zt.utils.RedisIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SpringTestApplications {

    private static final ExecutorService es = Executors.newFixedThreadPool(500);

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private RedisIdGenerator redisIdGenerator;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 同步店铺数据到redis，并设置逻辑过期时间
     */
    @Test
    public void syncShopData2Redis() {
        for (int i = 1; i < 14; i++) {
            shopService.saveData2Redis((long) i, 1800);
        }
    }


    @Test
    public void testIdGenerator() {
        long l = redisIdGenerator.nextId();
        System.out.println("l = " + l);
    }

    @Test
    public void testShop() {
        Page<Shop> page = new Page<>();
        page.setCurrent(1);
        page.setSize(5);
        Page<Shop> page1 = shopService.page(page);
        System.out.println("page1 = " + page1);
    }


    @Test
    public void testBatchIdGenerator() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(500);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdGenerator.nextId("order:");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = DateUtil.current();
        for (int i = 0; i < 500; i++) {
            es.submit(task);
        }
        latch.await(10, TimeUnit.SECONDS);
        long end = DateUtil.current();
        Console.print("time = {}\n",end - begin);
    }


    /**
     * 同步店铺地理位置数据到redis
     */
    @Test
    public void testSyncShopGeo() {
        List<Shop> shopList = shopService.list();
        Map<Long, List<Shop>> map = shopList.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        // 遍历所有
        map.forEach((typeId, shops) -> {
            // 使用GeoLocation<String> 保存地理数据，泛型为店铺id
            List<RedisGeoCommands.GeoLocation<String>> locationList = new ArrayList<>();
            String key = RedisConstants.SHOP_GEO_KEY + typeId;
            shops.forEach(shop -> {
                RedisGeoCommands.GeoLocation<String> location = new RedisGeoCommands.GeoLocation<>(String.valueOf(shop.getId()),
                        new Point(shop.getX(), shop.getY()));
                locationList.add(location);
            });
            stringRedisTemplate.opsForGeo().add(key, locationList);
        });
    }
}
