package com.zt.concurrent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZT
 */
public class SynchronizedTest {

    private static int count = 0;

    private static final Object lock = new Object();

    /**
     * 多线程访问共享资源 出现线程安全问题
     */
    @Test
    public void twoThreadOpSameVariable() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                count++;
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                count--;
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("answer = " + count);// 输出值不确定
    }


    /**
     * synchronized关键字解决线程安全问题
     */
    @Test
    public void twoThreadOpSameVariableUseSynchronizedSolution() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                synchronized (lock) {
                    count++;
                }
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                synchronized (lock) {
                    count--;
                }
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("answer = " + count);// 输出0
    }


    /**
     * 原子变量
     */
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 使用原子类解决线程安全问题
     */
    @Test
    public void twoThreadOpSameVariableUseAtomicInteger() throws InterruptedException{
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                atomicInteger.incrementAndGet();
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                atomicInteger.decrementAndGet();
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("answer = " + atomicInteger);// 输出0
    }

    /**
     * 非线程安全的list操作，list被多个线程共享
     */
    static class SharedListUnSafeThread {
        private List<String> list = new ArrayList<>();

        public void method1(int times) {
            for (int i = 0; i < times; i++) {
                method2();
                method3();
            }
        }

        private void method2() {
            list.add("0");
        }

        private void method3() {
            list.remove(0);
        }
    }

    /**
     * 可能出现 java.lang.IndexOutOfBoundsException: Index: 0, Size: 1
     * 原因：当两个线程同时进行add操作，size++不是原子操作，add两次但size可能为1
     */
    @Test
    public void opSharedUnSafeList() {
        SharedListUnSafeThread t1 = new SharedListUnSafeThread();
        new Thread(() -> t1.method1(10000), "t1").start();
        new Thread(() -> t1.method1(10000), "t2").start();
    }


    /**
     * 线程安全的list操作，list是局部变量，不是共享数据
     */
    static class SharedListSafeThread {

        public void method1(int times) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < times; i++) {
                method2(list);
                method3(list);
            }
        }

        private void method2(List<String> list) {
            list.add("0");
        }

        private void method3(List<String> list) {
            list.remove(0);
        }
    }

    /**
     * 不出现异常
     */
    @Test
    public void opSharedSafeList() {
        SharedListSafeThread t1 = new SharedListSafeThread();
        new Thread(() -> t1.method1(100000), "t1").start();
        new Thread(() -> t1.method1(100000), "t2").start();
    }


}
