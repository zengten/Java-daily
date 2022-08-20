package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author ZT
 */
@Slf4j
public class WaitNotifyTest {

    private static final Object obj = new Object();

    /**
     * Object wait() and notify() method 都需要在 synchronized 内 调用
     * wait() 无限等待  wait(long timeout) 等 timeout 毫秒 wait(long timeout, int nanos) 等 timeout + 1 毫秒
     * notify() 唤醒一个等待的线程  notifyAll() 唤醒全部等待的线程
     */
    @Test
    public void waitNotifyTest() {
        new Thread(() -> {
            synchronized (obj) {
                log.info("t1 running");
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("t1 wake...");
            }
        }, "t1").start();

        new Thread(() -> {
            synchronized (obj) {
                log.info("t2 running");
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("t2 wake...");
            }
        }, "t2").start();

        ThreadUtil.sleep(2000);
        log.info("main notify start");
        synchronized (obj) {
            obj.notifyAll();// 唤醒全部等待的线程
//            obj.notify(); // 唤醒一个等待的线程
        }
        log.info("main notify end");
    }


    /**
     * sleep() 不会释放锁   wait() 会释放锁
     */
    @Test
    public void sleepTest() {
        new Thread(() -> {
            synchronized (obj) {
                log.info("t1 running");
                ThreadUtil.sleep(5000);
//                try {
//                    obj.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                log.info("t1 wake...");
            }
        }, "t1").start();
        ThreadUtil.sleep(1000);
        synchronized (obj) {
            log.info("main 获得锁");
        }
    }

    /**
     * 牛奶面包
     */
    private static boolean hasMilk = false;

    private static boolean hasBread = false;

    /**
     * 测试notify 和 wait
     */
    @Test
    public void waitNotifyDemoTest() {
        new Thread(() -> {
            synchronized (obj) {
                log.info("t1 running");
                while (!hasMilk) {
                    log.info("t1没有牛奶，需要休息");
                    synchronized (obj) {
                        try {
                            obj.wait(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(hasMilk) {
                    log.info("t1开始干活...");
                }
            }
        }, "t1").start();

        new Thread(() -> {
            synchronized (obj) {
                log.info("t2 running");
                while (!hasBread) {
                    log.info("t2没有面包，需要休息");
                    synchronized (obj) {
                        try {
                            obj.wait(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(hasBread) {
                    log.info("t2开始干活...");
                }
            }
        }, "t1").start();
        ThreadUtil.sleep(5000);
        synchronized (obj) {
            hasBread = true;
            hasMilk = true;
            log.info("main添加面包和牛奶");
            obj.notifyAll();
        }
    }

}
