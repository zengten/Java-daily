package com.zt;

import com.zt.utils.RedisIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class StringTemplateTest {

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
            if(index == 0) {
                stringRedisTemplate.opsForHyperLogLog().add(key, userIds);
            }
        }
        Long size = stringRedisTemplate.opsForHyperLogLog().size(key);
        // 997938  误差大约 0.2%
        System.out.println("size = " + size);
    }

}
