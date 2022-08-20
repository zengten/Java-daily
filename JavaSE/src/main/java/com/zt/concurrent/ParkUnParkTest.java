package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

/**
 * @author ZT
 */
@Slf4j
public class ParkUnParkTest {


    /**
     * park() 暂停当前线程执行 unPark(t) 恢复t线程执行
     */
    @Test
    public void parkUnParkTest() {
        Thread t1 = new Thread(() -> {
            log.info("t1 start....");
            LockSupport.park();
            log.info("t1 wake....");
        }, "t1");
        t1.start();
        ThreadUtil.sleep(3000);
        log.info("main unPark...");
        LockSupport.unpark(t1);
    }


    /**
     * unpark 可以在park之前执行，但是必须在t1线程start之后
     * notify 不能在wait之前执行
     */
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            ThreadUtil.sleep(2000);
            log.info("t1 start....");
            LockSupport.park();
            log.info("t1 wake....");
        }, "t1");
        t1.start();
        ThreadUtil.sleep(1000);
        LockSupport.unpark(t1);
        log.info("main unPark...after");
    }
}
