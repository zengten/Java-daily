package com.zt.concurrent;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author ZT
 */
@Slf4j
public class AccountTransferTest {

    private static class Account {

        private int count;

        public Account(int count) {
            this.count = count;
        }

        /**
         * 这里是临界区代码需要线程安全处理
         * 由于存在两个 Account 进行转账，使用 synchronized(this)
         * 或者方法加 synchronized 均无法实现线程安全
         * 需要使用类对象锁才行
         * synchronized(Account.class) {
         *     if(count >= money) {
         *         count -= money;
         *         target.count += money;
         *     }
         * }
         * @param target 被转账用户
         * @param money 转账金额
         */
        public void transfer(Account target, int money) {
            if(count >= money) {
                count -= money;
                target.count += money;
            }
        }
    }

    /**
     * 模拟转账
     */
    @Test
    public void twoAccountTransfer() throws InterruptedException {
        Account a1 = new Account(1000);
        Account a2 = new Account(1000);
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                a1.transfer(a2, RandomUtil.randomInt(100));
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                a2.transfer(a1, RandomUtil.randomInt(100));
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        // 结果输出不是2000，存在线程安全问题
        log.info("账户总金额为{}",a1.count + a2.count);
    }
}
