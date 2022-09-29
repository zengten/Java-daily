package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock Feature
 *
 * @author ZT
 * @version 1.0
 * @date 2022/8/23 21:42
 */
@Slf4j
public class ReentrantLockTest {

    private static ReentrantLock lock = new ReentrantLock();

    /**
     * test ReentrantLock lock and unlock
     */
    @Test
    public void testReentrantLockState() {
        lock.lock();
        try {
            log.info("beforeUnlock state {}", lock.isLocked());
            log.info("do something content");
        }finally {
            lock.unlock();
        }
        log.info("afterUnlock state {}", lock.isLocked());
    }

    /**
     * test ReentrantLock Re-Reentrant Feature
     */
    @Test
    public void testReentrantFeature() {
        lock.lock();
        try {
            log.info("testReentrantFeature execute...");
            method1();
        } finally {
            lock.unlock();
            log.info("testReentrantFeature unlock...");
        }
    }

    private void method1() {
        lock.lock();
        try {
            log.info("method1 execute...");
            method2();
        } finally {
            lock.unlock();
            log.info("method1 unlock...");
        }
    }

    private void method2() {
        lock.lock();
        try {
            log.info("method2 execute ...");
        } finally {
            lock.unlock();
            log.info("method2 unlock...");
        }
    }

    /**
     * 测试 ReentrantLock 可被打断
     */
    @Test
    public void testReentrantInterrupt() {
        Thread t1 = new Thread(() -> {
            try {
                lock.lockInterruptibly();
                log.info("lock Interrupt has execute...");
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("没有获取到锁返回...");
                return;
            }
            try {
                log.info("t1获取到锁...");
            } finally {
                lock.unlock();
            }
        }, "t1");
        t1.start();
        lock.lock();
        log.info("main获取到锁...");
        ThreadUtil.sleep(1000);
        log.info("main thread interrupt t1...");
        t1.interrupt();
    }

}
