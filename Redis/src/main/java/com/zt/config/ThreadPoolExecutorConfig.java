package com.zt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置线程池对象
 */
@Configuration
@Slf4j
public class ThreadPoolExecutorConfig {


    /**
     * 4核心，队列长度1000的线程池对象
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                4,
                4,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000),
                Executors.defaultThreadFactory(),
                (task, executor) -> {
                    log.error("线程池任务溢出，{}, {}", task, executor);
                });
    }
}