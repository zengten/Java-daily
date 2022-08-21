package com.zt.concurrent;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.zt.concurrent.GuardSuspensionTest.GuardObjectV2;

/**
 * 同步模式：  保护性暂停-多任务版
 *
 * @author ZT
 */
@Slf4j
public class MultiTaskGuardSuspensionTest {

    /**
     * 信箱
     */
    private static class MailBoxes {
        /**
         * 信箱集合 Hashtable线程安全
         */
        private static Map<Integer, GuardObjectV2> boxes = new Hashtable<>();

        private static AtomicInteger id = new AtomicInteger(0);

        public static Integer generateId() {
            return id.getAndIncrement();
        }

        public static GuardObjectV2 getGuardObjectById(Integer id) {
            return boxes.remove(id);
        }

        public static GuardObjectV2 generateGuardObject() {
            GuardObjectV2 guardObject = new GuardObjectV2(generateId());
            log.info("generateGuardObject success, id = {}", guardObject.getId());
            boxes.put(guardObject.getId(), guardObject);
            return guardObject;
        }

    }

    /**
     * 收信人
     */
    private static class Person implements Runnable {

        @Override
        public void run() {
            GuardObjectV2 guardObject = MailBoxes.generateGuardObject();
            log.info("person {} 开始收信.....", guardObject.getId());
            Object obj = guardObject.getObj(2000);
            log.info("person {} 收到信, 内容为 {}", guardObject.getId(), obj);
        }
    }

    /**
     * 邮递员
     */
    private static class PostMan implements Runnable {

        private int id;

        private String content;

        public PostMan(int id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public void run() {
            // 送信耗时
            ThreadUtil.sleep(RandomUtil.randomInt(800));
            GuardObjectV2 guardObject = MailBoxes.getGuardObjectById(id);
            log.info("邮递员开始送信,id = {}, mail = {}", id, content);
            guardObject.complete(content);
            log.info("邮递员完成送信,id = {}, mail = {}", id, content);
        }
    }


    @Test
    public void multiTaskGuardSuspensionTest() {
        for (int i = 0; i < 3; i++) {
            new Thread(new Person(), "t" + i).start();
        }
        for (int i = 0; i < 3; i++) {
            new Thread(new PostMan(i, "内容" + i), "t" + i).start();
        }
        ThreadUtil.sleep(3000);
    }

}
