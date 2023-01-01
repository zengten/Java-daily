package mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

@Slf4j
public class ConcurrentConsumerTest {

    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer("group1");
        pushConsumer.setNamesrvAddr("192.168.6.44:9876;192.168.6.44:9886");
        // 订阅主题，指定tag/多个tag表达式, 消息过滤：只消费 i>5 的消息
        // 使用MessageSelector.bySql方法  需要broker配置enablePropertyFilter=true
//        pushConsumer.subscribe("testBatchTopic", MessageSelector.bySql("i > 5"));
        pushConsumer.subscribe("testSyncTopic", "*");
        // 默认集群模式（负载均衡），另外还有广播模式
        pushConsumer.setMessageModel(MessageModel.CLUSTERING);
        // 设置一次消费的消息数量
        pushConsumer.setConsumeMessageBatchMaxSize(1);
        // 推送消费模式 监听器
        // MessageListenerOrderly和MessageListenerConcurrently 两个重载方法要带上参数类型
        // 同时要返回消费是否成功的状态
        pushConsumer.registerMessageListener((List<MessageExt> messageExtList, ConsumeConcurrentlyContext context) -> {
                    System.out.println("threadName = " + Thread.currentThread().getName());
                    messageExtList.forEach(item -> {
                        System.out.println("item = " + item);
                        String body = new String(item.getBody());
                        System.out.println("body = " + body);
                    });
                    System.out.println("context = " + context);
                    // 设置 consumeMessageBatchMaxSize 每次消费的消息数
                    log.info("提交消息数{}", messageExtList.size());
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
        );
        pushConsumer.start();
        System.out.println("pushConsumer start......");
    }
}
