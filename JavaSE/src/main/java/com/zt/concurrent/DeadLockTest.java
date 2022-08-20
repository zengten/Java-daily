package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 死锁
 * @author ZT
 */
@Slf4j
public class DeadLockTest {


    public static void main(String[] args) {
        final Object obj1 = new Object();
        final Object obj2 = new Object();
        // t1获得obj1锁，等待获取obj2锁，而t2获得obj2锁，等待获取obj1锁
        // 两个线程各获得资源而相互等待的现象
        new Thread(() -> {
            synchronized (obj1) {
                log.info("t1获得obj1，正在请求obj2");
                ThreadUtil.sleep(2000);
                synchronized (obj2) {
                    log.info("t1获得obj2");
                }
            }
        }, "t1").start();
        new Thread(() -> {
            synchronized (obj2) {
                log.info("t2获得obj2，正在请求obj1");
                ThreadUtil.sleep(2000);
                synchronized (obj1) {
                    log.info("t1获得obj1");
                }
            }
        }, "t2").start();
    }


}
