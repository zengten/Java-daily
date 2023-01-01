package mq;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

@Slf4j
public class OrderlyConsumerTest {

    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer("group1");
        pushConsumer.setNamesrvAddr("192.168.6.44:9876;192.168.6.44:9886");
        // 订阅主题，指定tag/多个tag表达式
        pushConsumer.subscribe("testOrderlyTopic1", "*");
        // 设置消费开始点   在队首
        pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        // 推送消费模式 监听器
        // MessageListenerOrderly和MessageListenerConcurrently 两个重载方法要带上参数类型
        // 同时要返回消费是否成功的状态
        pushConsumer.registerMessageListener((List<MessageExt> messageExtList, ConsumeOrderlyContext context) -> {
                    log.info("threadName = " + Thread.currentThread().getName());
                    messageExtList.forEach(item -> {
                        String body = new String(item.getBody());
                        log.info("body = {} ", body);
                    });
                    ThreadUtil.sleep(2000);
                    log.info("消费成功{}条", messageExtList.size());
                    return ConsumeOrderlyStatus.SUCCESS;
                }
        );
        pushConsumer.start();
        System.out.println("pushConsumer start......");
    }
}
