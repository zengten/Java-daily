package com.zt.concurrent;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

/**C
 * 交替打印ABC 出处 lintCode
 * 存在问题  FIXME ABC顺序不一定  有可能ACB
 * @author ZT
 */
@Slf4j
public class AlertPrintThreeCharTest {

    static class Solution implements Runnable {

        private String name;

        private Object prev;

        private Object self;

        private Integer printCount;

        public Solution(String name, Object prev, Object self, Integer printCount) {
            this.name = name;
            this.prev = prev;
            this.self = self;
            this.printCount = printCount;
        }

        @Override
        public void run() {
            while (printCount > 0) {
                synchronized (prev) {
                    synchronized (self) {
                        log.info(name);
                        printCount--;
                        self.notify();
                    }
                    try {
                        // 线程结束
                        if (printCount == 0) {
                            prev.notify();
                        } else {
                            // 线程等待
                            prev.wait();
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Try.run(() -> function(3));
    }

    /**
     * 执行三个线程
     * @param printCount
     */
    private static void function(int printCount) {
        Object oa = new Object();
        Object ob = new Object();
        Object oc = new Object();
        // 建立三个线程
        Solution solutionA = new Solution("A", oc, oa, printCount);
        Solution solutionB = new Solution("B", oa, ob, printCount);
        Solution solutionC = new Solution("C", ob, oc, printCount);
        new Thread(solutionA).start();
        new Thread(solutionB).start();
        new Thread(solutionC).start();
    }
}