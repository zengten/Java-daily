package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.LockSupport;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/8/16 19:28
 */
@Slf4j
public class BiasedLockTest {


    /**
     * 如果添加参数 VM option: -XX:-UseBiasedLocking 会禁用偏向锁
     */
    @Test
    public void delayBiasedLockTest() throws InterruptedException {
        String s = ClassLayout.parseInstance(new Object()).toPrintable();
        // 输出 001
        log.info(s);
        // 延迟加载偏向锁
        Thread.sleep(5000);
        ClassLayout classLayout = ClassLayout.parseInstance(new Object());
        String s1 = classLayout.toPrintable();
        // 输出101
        log.info(s1);
    }


    /**
     * -XX:BiasedLockingStartupDelay=0 会让偏向锁立即生效,无延迟
     */
    @Test
    public void noDelayBiasedLockTest() {
        String s = ClassLayout.parseInstance(new Object()).toPrintable();
        // 输出 101
        log.info(s);
    }

    /**
     * 调用hashcode()使得偏向锁撤销，obj mark word 已存放 hashcode，没地方存储threadId
     */
    @Test
    public void biasedLockCancelTest() {
        Object obj = new Object();
        obj.hashCode();
        String s = ClassLayout.parseInstance(obj).toPrintable();
        // 输出 001
        log.info(s);
    }


    /**
     * obj make word 持有当前线程的id 偏向当前线程
     */
    @Test
    public void biasedLockMarkWordTest() {
        Object obj = new Object();
        // 101可偏向状态 biasable
        log.info(ClassLayout.parseInstance(obj).toPrintable());
        synchronized (obj) {
            // obj mark word 持有当前线程的偏向锁 biased
            log.info(ClassLayout.parseInstance(obj).toPrintable());
        }
        // obj mark word 持有当前线程的偏向锁 biased
        log.info(ClassLayout.parseInstance(obj).toPrintable());
    }


    /**
     * -XX:BiasedLockingStartupDelay=0 测试偏向锁撤销  重偏向其它线程
     */
    @Test
    public void biasedLockCancelOtherTest() {
        final Object object = new Object();
        Thread t1 = new Thread(() -> {
            log.info("t1 before....");
            // biasable
            log.info(ClassLayout.parseInstance(object).toPrintable());
            synchronized (object) {
                // biased t1 thread
                log.info(ClassLayout.parseInstance(object).toPrintable());
            }
            log.info("t1 after....");
            // biased t1 thread
            log.info(ClassLayout.parseInstance(object).toPrintable());
            synchronized (BiasedLockTest.class) {
                BiasedLockTest.class.notify();
            }
        });
        Thread t2 = new Thread(() -> {
            synchronized (BiasedLockTest.class) {
                try {
                    BiasedLockTest.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("t2 before....");
            // biased t1 thread, object记录上次的偏向线程
            log.info(ClassLayout.parseInstance(object).toPrintable());
            synchronized (object) {
                // thin lock 轻量级锁 00
                log.info(ClassLayout.parseInstance(object).toPrintable());
            }
            log.info("t2 after....");
            // non-biasable 无偏向状态 001
            log.info(ClassLayout.parseInstance(object).toPrintable());
        });
        t1.start();
        t2.start();
        // 等t1 t2 执行完成
        ThreadUtil.sleep(7000);
    }


    /**
     * -XX:BiasedLockingStartupDelay=0   测试批量偏向锁撤销
     * 批量撤销达19次，之后会重新偏向新的线程
     */
    @Test
    public void biasedLockBatchCancel20Test() {
        List<Object> objectList = new Vector<>();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 25; i++) {
                Object obj = new Object();
                objectList.add(obj);
                // biasable
                log.info(i + ">>>>>>>t1 before......." + ClassLayout.parseInstance(obj).toPrintable());
                synchronized (obj) {
                    // biased t1 thread
                    log.info(ClassLayout.parseInstance(obj).toPrintable());
                }
                // biased t1 thread
                log.info(i + ">>>>>>>t1 after......." + ClassLayout.parseInstance(obj).toPrintable());
            }
            synchronized (objectList) {
                objectList.notify();
            }
        });
        t1.start();
        Thread t2 = new Thread(() -> {
            synchronized (objectList) {
                try {
                    objectList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < 25; i++) {
                Object obj = objectList.get(i);
                // biasable t1
                log.info(i + ">>>>>>>t2 before......." + ClassLayout.parseInstance(obj).toPrintable());
                synchronized (obj) {
                    // 前18次 thin lock 之后obj进行重偏向  t2的偏向锁
                    log.info(ClassLayout.parseInstance(obj).toPrintable());
                }
                // non-biasable
                log.info(i + ">>>>>>>t2 after......." + ClassLayout.parseInstance(obj).toPrintable());
            }
        });
        t2.start();
        ThreadUtil.sleep(10000);
    }

    static Thread t1, t2, t3;

    /**
     * -XX:BiasedLockingStartupDelay=0
     * 偏向锁批量撤销达39次，jvm就会觉得不应该使用偏向锁，所有对象不再使用偏向锁
     */
    @Test
    public void biasedLockBatchCancel40Test() {
        List<Object> objectList = new Vector<>();
        int nums = 30;
        t1 = new Thread(() -> {
            for (int i = 0; i < nums; i++) {
                Object obj = new Object();
                objectList.add(obj);
                // biasable
                log.info(i + ">>>>>>>t1 before......." + ClassLayout.parseInstance(obj).toPrintable());
                synchronized (obj) {
                    // biased t1 thread
                    log.info(ClassLayout.parseInstance(obj).toPrintable());
                }
                // biased t1 thread
                log.info(i + ">>>>>>>t1 after......." + ClassLayout.parseInstance(obj).toPrintable());
            }
            LockSupport.unpark(t2);
        });
        t1.start();
        t2 = new Thread(() -> {
            LockSupport.park();
            for (int i = 0; i < nums; i++) {
                Object obj = objectList.get(i);
                // biasable t1
                log.info(i + ">>>>>>>t2 before......." + ClassLayout.parseInstance(obj).toPrintable());
                synchronized (obj) {
                    // 前18次 thin lock 之后obj进行重偏向  t2的偏向锁
                    log.info(ClassLayout.parseInstance(obj).toPrintable());
                }
                // non-biasable
                log.info(i + ">>>>>>>t2 after......." + ClassLayout.parseInstance(obj).toPrintable());
            }
            LockSupport.unpark(t3);
        });
        t2.start();

        t3 = new Thread(() -> {
            LockSupport.park();
            for (int i = 0; i < nums; i++) {
                Object obj = objectList.get(i);
                // biasable t2
                log.info(i + ">>>>>>>t3 before......." + ClassLayout.parseInstance(obj).toPrintable());
                synchronized (obj) {
                    // 全部是 thin lock 因为偏向锁撤销达40次，jvm不再使用偏向锁
                    log.info(ClassLayout.parseInstance(obj).toPrintable());
                }
                // non-biasable
                log.info(i + ">>>>>>>t3 after......." + ClassLayout.parseInstance(obj).toPrintable());
            }
        });
        t3.start();
        ThreadUtil.sleep(10000);
    }
}
