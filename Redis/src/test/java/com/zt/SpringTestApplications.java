package com.zt;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zt.entity.Shop;
import com.zt.service.impl.ShopServiceImpl;
import com.zt.utils.RedisIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SpringTestApplications {

    private static final ExecutorService es = Executors.newFixedThreadPool(500);


    @Autowired
    private ShopServiceImpl shopService;


    @Autowired
    private RedisIdGenerator redisIdGenerator;


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

}
