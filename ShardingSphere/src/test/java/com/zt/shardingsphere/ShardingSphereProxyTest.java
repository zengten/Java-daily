package com.zt.shardingsphere;

import com.zt.shardingsphere.dao.OrderMapper;
import com.zt.shardingsphere.dao.UserMapper;
import com.zt.shardingsphere.entity.Order;
import com.zt.shardingsphere.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/28 21:01
 */
@SpringBootTest
public class ShardingSphereProxyTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 测试 主从数据库
     */
    @Test
    public void testMasterSlaveQuery() {
        List<User> users = userMapper.selectList(null);
        System.out.println("users = " + users);
        List<User> users1 = userMapper.selectList(null);
        System.out.println("users1 = " + users1);
    }


    /**
     * 测试垂直分片和水平分片数据库
     */
    @Test
    public void testVerticalQuery() {
        User user = userMapper.selectById(1);
        System.out.println("user = " + user);
        Order order = orderMapper.selectById(1);
        System.out.println("order = " + order);
    }
}
