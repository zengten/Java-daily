package com.zt;

import com.zt.utils.RedisIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Slf4j
public class StringRedisTemplateTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisIdGenerator redisIdGenerator;

    /**
     * 测试UV数据访问统计
     */
    @Test
    public void testHyperLog() {
        String key = "hyperLog";
        String[] userIds = new String[1000];
        for (int i = 1; i < 1000000; i++) {
            String userId = "user_" + redisIdGenerator.nextId();
            int index = i % 1000;
            userIds[index] = userId;
            if (index == 0) {
                stringRedisTemplate.opsForHyperLogLog().add(key, userIds);
            }
        }
        Long size = stringRedisTemplate.opsForHyperLogLog().size(key);
        // 997938  误差大约 0.2%
        System.out.println("size = " + size);
    }

    /**
     * 测试 hash
     */
    @Test
    public void testHash() {
        stringRedisTemplate.opsForHash().put("testHash", "k1", "v1");
        Boolean success = stringRedisTemplate.opsForHash().putIfAbsent("testHash", "k1", "v1");
        log.info("success = {}", success);
        Object value = stringRedisTemplate.opsForHash().get("testHash", "k1");
        log.info("value = {}", value);
        Map<String, Object> map = new HashMap<>();
        map.put("k2", "v2");
        map.put("k3", "v3");
        map.put("cc", "vv");
        stringRedisTemplate.opsForHash().putAll("testHash", map);
        List<Object> testHash = stringRedisTemplate.opsForHash().values("testHash");
        log.info("all Value = {}", testHash);
        Long size = stringRedisTemplate.opsForHash().size("testHash");
        log.info("size = {}", size);
        Cursor<Map.Entry<Object, Object>> scan = stringRedisTemplate.opsForHash().scan(
                "testHash",
                ScanOptions.scanOptions().match("k*").build());
        scan.forEachRemaining(entry -> {
            Object key = entry.getKey();
            Object value1 = entry.getValue();
            log.info("scan key = {}, value = {}", key, value1);
        });
        Long incrementResult = stringRedisTemplate.opsForHash().increment("testHash", "num", 66);
        log.info("incrementResult = {}", incrementResult);
        // 非数值value调用increment会报错
        // Long incrementResultStr = stringRedisTemplate.opsForHash().increment("testHash", "k1", 66);
    }

    @Test
    public void testStreamAdd() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", "22");
        // 两种 xadd 命令使用方式
        RecordId recordId = stringRedisTemplate.opsForStream().add("testStream0", map);
        stringRedisTemplate.opsForStream().add(MapRecord.create("testStream1", map));
        Long sequence = recordId.getSequence();
        Long timestamp = recordId.getTimestamp();
        String value = recordId.getValue();
        // sequence = 0, timestamp = 1671027635825, value = 1671027635825-0, 生成id的组成部分，时间戳+自增
        log.info(">>>>>sequence = {}, timestamp = {}, value = {}", sequence, timestamp, value);
    }

    @Test
    public void testStreamDelete() {
        // 只能根据id删除
        stringRedisTemplate.opsForStream().delete("testStream0", "1671028130434-0");
        stringRedisTemplate.opsForStream().delete("testStream1", RecordId.of("1671028130436-0"));

    }

    @Test
    public void testStreamGroup() {
        Boolean del0 = stringRedisTemplate.opsForStream().destroyGroup("testStream0", "g0");
        Boolean del1 = stringRedisTemplate.opsForStream().destroyGroup("testStream0", "g1");
        // true
        log.info(">>>del0 = {}, del1 = {}", del0, del1);
        String group0 = stringRedisTemplate.opsForStream().createGroup("testStream0", "g0");
        String group1 = stringRedisTemplate.opsForStream().createGroup("testStream0", ReadOffset.latest(), "g1");
//        log.info(">>>group0 = {}, group1 = {}, groups = {}", group0, group1, groups);
        // group0 = OK, group1 = OK
        log.info(">>>group0 = {}, group1 = {}", group0, group1);
        StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups("testStream0");
        // groups = XInfoGroups[XInfoStream{name=g0, consumers=0, pending=0, last-delivered-id=1671028176660-0, entries-read=null, lag=0},
        // XInfoStream{name=g1, consumers=0, pending=0, last-delivered-id=1671028176660-0, entries-read=null, lag=0}]
        log.info(">>>groups = {}", groups.toString());
    }


}
