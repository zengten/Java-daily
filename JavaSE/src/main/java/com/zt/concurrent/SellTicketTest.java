package com.zt.concurrent;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * @author ZT
 */
@Slf4j
public class SellTicketTest {

    /**
     * 卖票窗口
     */
    private static class TicketWindow {
        private int count;

        public TicketWindow(int count) {
            this.count = count;
        }

        /**
         * 卖票并返回卖出票数
         * 可能超卖
         * 改为 public synchronized int sellAmount 可解决线程安全问题
         */
        public int sellAmount(int amount) {
            if(count >= amount) {
                count -= amount;
                return amount;
            } else {
                return 0;
            }
        }
    }

    @Test
    public void multiThreadSellTicket() {
        TicketWindow window = new TicketWindow(1000);
        List<Integer> totalSellCount = new Vector<>();
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int amount = window.sellAmount(RandomUtil.randomInt(5));
                totalSellCount.add(amount);
            });
            t.start();
            threadList.add(t);
        }
        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        int sum = totalSellCount.stream().mapToInt(Integer::intValue).sum();
        // 可能超过总票数 1000
        log.info("卖出票数 = {}", sum);
    }
}
