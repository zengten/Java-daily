package com.zt.shardingsphere;

import com.zt.shardingsphere.dao.OrderMapper;
import com.zt.shardingsphere.dao.UserMapper;
import com.zt.shardingsphere.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/21 21:25
 */
@SpringBootTest
public class ReadWriteTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 插入数据库使用master
     */
    @Test
    public void testInsert() {
        User user = new User();
        user.setUname("李四");
        userMapper.insert(user);
    }

    /**
     * 从数据库轮询：user1查询通过slave1，user2查询通过slave2
     */
    @Test
    public void testQuery() {
        User user1 = userMapper.selectById(9);
        System.out.println("user1 = " + user1);
        User user2 = userMapper.selectById(10);
        System.out.println("user2 = " + user2);
    }

    /**
     * shardingSphere主从模型中，当存在事务时，数据库操作均使用主库
     * 不添加@Transactional：insert对主库操作，select对从库操作
     * 添加@Transactional：则insert和select均对主库操作
     */
    @Transactional
    @Test
    public void testTransactional() {
        User user = new User();
        user.setUname("赵六");
        userMapper.insert(user);
        List<User> users = userMapper.selectList(null);
        System.out.println("users = " + users);
    }

}
