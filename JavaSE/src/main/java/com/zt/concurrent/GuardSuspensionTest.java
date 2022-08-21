package com.zt.concurrent;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.Objects;

/**
 * 同步模式：  保护性暂停-单任务版
 * @author ZT
 */
@Slf4j
public class GuardSuspensionTest {

    private static class GuardObjectV1 {

        private Object obj;

        public Object getObj() {
            synchronized (this) {
                while (Objects.isNull(obj)) {
                    try {
                        log.info("obj is null, waiting");
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return obj;
            }
        }

        public void complete(Object obj) {
            synchronized (this) {
                this.obj = obj;
                this.notifyAll();
            }
        }
    }


    /**
     * 保护性暂停测试
     */
    @Test
    public void guardSuspensionV1Test() {
        GuardObjectV1 objectV1 = new GuardObjectV1();
        new Thread(() -> {
            Object obj = objectV1.getObj();
            log.info("response = {}", obj);
        }, "t1").start();
        new Thread(() -> {
            objectV1.complete(Arrays.asList("a", "b", "c"));
            log.info("t2 complete...");
        }, "t2").start();
        ThreadUtil.sleep(2000);
    }

    static class GuardObjectV2 {

        /**
         * GuardObject 标识
         */
        private Integer id;

        public Integer getId() {
            return id;
        }

        public GuardObjectV2(Integer id) {
            this.id = id;
        }

        public GuardObjectV2() {
        }

        private Object obj;

        public Object getObj(int timeout) {
            synchronized (this) {
                TimeInterval timer = DateUtil.timer();
                timer.start();
                long curInterval = 0;
                while (Objects.isNull(obj)) {
                    // 超过等待时间
                    curInterval = timer.interval();
                    if(curInterval > timeout) {
                        break;
                    }
                    try {
                        log.info("obj is null, waiting");
                        this.wait(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("interval = {} ms", timer.interval());
                return obj;
            }
        }

        public void complete(Object obj) {
            synchronized (this) {
                this.obj = obj;
                this.notifyAll();
            }
        }
    }

    /**
     * 保护性暂停  超时等待
     */
    @Test
    public void guardSuspensionV2Test() {
        GuardObjectV2 objectV2 = new GuardObjectV2();
        new Thread(() -> {
            Object obj = objectV2.getObj(2000);
            log.info("response = {}", obj);
        }, "t1").start();
        new Thread(() -> {
            log.info("t2 start send data...");
            ThreadUtil.sleep(3000);
            objectV2.complete(Arrays.asList("a", "b", "c"));
            log.info("t2 complete...");
        }, "t2").start();
        ThreadUtil.sleep(4000);
    }
}
