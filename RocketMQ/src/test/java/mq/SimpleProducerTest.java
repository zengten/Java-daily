package mq;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SimpleProducerTest {

    private DefaultMQProducer mqProducer;

    @Before
    public void before() throws Exception {
        mqProducer = new DefaultMQProducer("group1");
        // 如果多个mqNameServe，分号隔开 127.0.0.2:9876;127.0.0.3:9876
        mqProducer.setNamesrvAddr("192.168.6.44:9876;192.168.6.44:9886");
        mqProducer.start();
    }

    /**
     * 同步发送消息
     */
    @Test
    public void testSyncProducer() throws Exception {
        for (int i = 0; i < 5; i++) {
            Message message = new Message("testSyncTopic",
                    "tagA",
                    ("testSyncBody1" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult result = mqProducer.send(message);
            System.out.println("result = " + result);
        }
    }


    /**
     * 异步发送消息
     */
    @Test
    public void testAsyncProducer() throws Exception {
        for (int i = 0; i < 5; i++) {
            Message message = new Message("testAsyncTopic",
                    "tagB",
                    ("testAsyncBody" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            mqProducer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.println("sendResult = " + sendResult);
                }

                @Override
                public void onException(Throwable e) {
                    System.out.println("异常e = " + e);
                }
            });
        }
        ThreadUtil.sleep(2000);
    }


    /**
     * 单向异步发送，只发送不应答
     * 一般在微秒级别，适用于某些耗时非常短，但对可靠性要求并不高的场景，例如日志收集
     */
    @Test
    public void testOneWayProducer() throws Exception {
        Message message = new Message("testOneWayTopic",
                "tagC",
                "testAsyncBody".getBytes(RemotingHelper.DEFAULT_CHARSET));
        mqProducer.sendOneway(message);
        ThreadUtil.sleep(1000);
    }


    /**
     * 发送顺序消息
     * NameServer中的配置 orderMessageEnable 和 returnOrderTopicConfigToBroker 必须是 true
     * 如果上述任意一个条件不满足，则是保证可用性而不是严格顺序
     */
    @Test
    public void testOrderlyProducer() throws Exception {
        for (int i = 100; i < 200; i++) {
            // 业务id
            Integer orderId = i % 10;
            Message message = new Message("testOrderlyTopic1",
                    "tagD",
                    ("testOrderlyBody" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            // message是消息，MessageQueueSelector 队列选择器，arg是上述send接口中传入的Object对象，返回的是该消息需要发送到的队列
            SendResult result = mqProducer.send(message,
                    // 例如，将订单ID、用户ID作为分区键关键字，可实现同一终端用户的消息按照顺序处理，不同用户的消息无需保证顺序。
                    (List<MessageQueue> mqs, Message msg, Object arg) -> {
                        Integer id = (Integer) arg;
                        int index = id % mqs.size();
                        return mqs.get(index);
                    }
                    , orderId);
            System.out.println("result = " + result);
        }
        ThreadUtil.sleep(5000);
    }


    /**
     * 延迟消息
     * 如果将大量延时消息的定时时间设置为同一时刻，则到达该时刻后会有大量消息同时需要被处理，
     * 会造成系统压力过大，导致消息分发延迟，影响定时精度
     */
    @Test
    public void testDelayProducer() throws Exception {
        for (int i = 0; i < 3; i++) {
            Message message = new Message("testDelayTopic",
                    "tagE",
                    ("testDelayBody1" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            // 1~18个等级的延迟投递: 1s  5s  10s  30s  1min  2min
            message.setDelayTimeLevel(i + 3);
            SendResult result = mqProducer.send(message);
            System.out.println("result = " + result);
        }
        log.info("当前时间：{}", DateUtil.now());
        ThreadUtil.sleep(1000);
    }

    /**
     * 批量发送消息
     * 将一些消息聚成一批以后进行发送，可以增加吞吐率，并减少API和网络调用次数
     * 需要注意的是批量消息的大小不能超过 1MiB（否则需要自行分割），其次同一批 batch 中 topic 必须相同
     */
    @Test
    public void testBatchProducer() throws Exception {
        List<Message> messageList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Message message = new Message("testBatchTopic",
                    "tagF",
                    ("testBatchBody2" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            message.putUserProperty("i", String.valueOf(i));
            messageList.add(message);
        }
        SendResult result = mqProducer.send(messageList);
        System.out.println("result = " + result);
        ThreadUtil.sleep(1000);
    }


    @After
    public void after() {
        mqProducer.shutdown();
        System.out.println("关闭mqProducer...");
    }

}
