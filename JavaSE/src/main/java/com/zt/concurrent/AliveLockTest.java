package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author ZT
 * @version 1.0
 * @description: 活锁
 * @date 2022/8/20 20:21
 */
@Slf4j
public class AliveLockTest {

    private static int count = 10;

    /**
     * 两个线程互相改变对方的结束条件，最后谁也无法结束
     */
    @Test
    public void aliveLockTest() {
        new Thread(() -> {
           while (count > 0) {
               count--;
               log.info("t1线程进行count减一，count = {}", count);
               ThreadUtil.sleep(200);
           }
        }, "t1").start();
        new Thread(() -> {
            while (count < 20) {
                count++;
                log.info("t2线程进行count加一，count = {}", count);
                ThreadUtil.sleep(200);
            }
        }, "t2").start();
        ThreadUtil.sleep(10000);
    }
}
