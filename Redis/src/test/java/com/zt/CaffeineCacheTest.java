package com.zt;

import cn.hutool.core.thread.ThreadUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Duration;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/11/29 20:57
 */
@Slf4j
public class CaffeineCacheTest {

    /**
     * 基本用法
     */
    @Test
    public void testBasicOp() {
        Cache<String, String> cache = Caffeine.newBuilder().build();
        String name1 = cache.getIfPresent("name");
        System.out.println("before name = " + name1);// null
        cache.put("name", "张三");
        String name2 = cache.getIfPresent("name");
        System.out.println("after name = " + name2);// 张三
        Function1<String, String> f1 = (param) -> cache.get(param, key -> {
            log.info(">>>>>>create begin");
            return "create happy";
        });
        // create begin只调用一次
        System.out.println("happy1 = " + f1.apply("happy"));
        System.out.println("happy2 = " + f1.apply("happy"));
    }

    /**
     * 基于大小驱逐策略
     */
    @Test
    public void testEvictByNum() {
        Cache<String, String> cache = Caffeine.newBuilder().maximumSize(1).build();
        cache.put("name", "张三");
        cache.put("age", "22");
        String name = cache.getIfPresent("name");
        String age = cache.getIfPresent("age");
        System.out.println("name = " + name);// 张三
        System.out.println("age = " + age);// 22
        // 看上去缓存大小没生效，原因是缓存不是立即驱逐的
        ThreadUtil.sleep(1200);
        System.out.println("name = " + cache.getIfPresent("name"));// null
        System.out.println("age = " + cache.getIfPresent("age"));// 22
    }

    /**
     * 测试缓存时间驱逐策略
     */
    @Test
    public void testEvictByTime() {
        // 2秒后失效
        Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(2)).build();
        cache.put("name", "张三");
        cache.put("age", "22");
        String name = cache.getIfPresent("name");
        String age = cache.getIfPresent("age");
        System.out.println("name = " + name);// 张三
        System.out.println("age = " + age);// 22
        ThreadUtil.sleep(2200);
        // 2秒后均为null
        System.out.println("name = " + cache.getIfPresent("name"));// null
        System.out.println("age = " + cache.getIfPresent("age"));// null
    }
}
