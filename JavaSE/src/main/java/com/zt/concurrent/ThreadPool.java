package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class ThreadPool {
    private static ExecutorService pool;

    public static void main(String[] args) {
        //自定义拒绝策略
        pool = new ThreadPoolExecutor(
                1,
                1,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(8),
                Executors.defaultThreadFactory(),
                new RejectedExecutionHandler() {
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        System.out.println(r.toString() + "执行了拒绝策略");
                    }
                });


        pool.execute(() -> {
            while (true) {
                ThreadUtil.sleep(500);
                int randomInt = RandomUtil.randomInt(100);
                ThreadUtil.sleep(500);
                if((randomInt & 1) == 1) {
                    log.info("randomInt = {},奇数抛出异常！", randomInt);
                    throw new RuntimeException("hh");
                } else {
                    log.info("randomInt = {},偶数继续执行！", randomInt);
                }
            }
        });
    }

}

