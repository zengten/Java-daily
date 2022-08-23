package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.LinkedList;

/**
 * @author ZT
 * @version 1.0
 * @description: 异步模式-传统生产消费
 * @date 2022/8/21 19:19
 */
@Slf4j
public class ProducerConsumerTest {

    /**
     * 消息实体
     */
    @Data
    @AllArgsConstructor
    private static class Message {

        private Integer id;

        private Object value;
    }

    /**
     * 消息队列
     */
    private static class MessageQueue {

        private LinkedList<Message> queue = new LinkedList<>();

        /**
         * 队列长度
         */
        private int capacity;

        public MessageQueue(int capacity) {
            this.capacity = capacity;
        }

        public void consumer() {
            synchronized (this) {
                while (queue.isEmpty()) {
                    log.info("消息队列为空，消费者线程等待...");
                    Try.run(this::wait);
                }
                log.info("已消费消息，message = {}", queue.poll());
                this.notifyAll();
            }
        }

        public void producer(Message message) {
            synchronized(this) {
                log.info("消息队列长度size = {}, capacity = {}", queue.size(), capacity);
                while (queue.size() >= capacity) {
                    log.info("消息队列已满，生产者线程等待...");
                    Try.run(this::wait);
                }
                queue.offer(message);
                log.info("生产者线程已生产消息，message = {}", message);
                this.notifyAll();
            }
        }
    }

    @Test
    public void traditionalProducerConsumerTest() {
        MessageQueue messageQueue = new MessageQueue(2);
        for (int i = 0; i < 5; i++) {
            int j = i;
            new Thread(() -> messageQueue
                    .producer(new Message(j, "消息内容" + j)), "生产者" + i)
                    .start();
        }
        new Thread(() -> {
            while (true) {
                ThreadUtil.sleep(1000);
                messageQueue.consumer();
            }
        }, "消费者").start();
        ThreadUtil.sleep(10000);
    }
}
