package mq;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

@Slf4j
public class TransactionProducerTest {

    /**
     * 测试事务消息
     * 注意点：事务消息的生产组名称 ProducerGroupName不能随意设置。
     *        事务消息有回查机制，回查时Broker端如果发现原始生产者已经崩溃，
     *        则会联系同一生产者组的其他生产者实例回查本地事务执行情况以Commit或Rollback半事务消息
     *
     */
    public static void main(String[] args) throws Exception {
        TransactionMQProducer mqProducer = new TransactionMQProducer("group10");
        mqProducer.setNamesrvAddr("192.168.6.44:9876;192.168.6.44:9886");
        mqProducer.setTransactionListener(new TransactionListenerImpl());
        mqProducer.start();
        String[] tags = {"tagA", "tagB", "tagC"};
        for (int i = 0; i < 3; i++) {
            Message message = new Message("testTransactionTopic",
                    tags[i],
                    ("TransactionBody7" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult result = mqProducer.sendMessageInTransaction(message, null);
            log.info("{} result = {}", i, result);
            ThreadUtil.sleep(1000);
        }

//        mqProducer.shutdown();
    }


    /**
     * 消息监听器
     */
    static class TransactionListenerImpl implements TransactionListener {

        /**
         * 执行本地事务
         *
         * @param msg
         * @param arg
         * @return
         */
        @Override
        public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            if ("tagA".equals(msg.getTags())) {
                log.info("tagA commit");
                return LocalTransactionState.COMMIT_MESSAGE;
            } else if ("tagB".equals(msg.getTags())) {
                log.info("tagB rollback");
                return LocalTransactionState.ROLLBACK_MESSAGE;
            } else {
                log.info("other unknown");
                return LocalTransactionState.UNKNOW;
            }
        }

        /**
         * 回查本地事务
         * 回查时间间隔通过 broker的配置文件设置  默认60秒
         * transactionCheckInterval=60000
         * @param msg
         * @return
         */
        @Override
        public LocalTransactionState checkLocalTransaction(MessageExt msg) {
            log.info("check localTransaction {}", msg.getTags());
            return LocalTransactionState.COMMIT_MESSAGE;
        }
    }
}
