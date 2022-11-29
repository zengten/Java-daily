package com.zt.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zt.dto.Result;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/11/29 23:30
 */
@Configuration
public class CaffeineCacheConfig {

    @Bean
    public Cache<String, Result> resultCaffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .build();
    }

}
