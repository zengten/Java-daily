package com.zt.shardingsphere;

import com.zt.shardingsphere.dao.OrderMapper;
import com.zt.shardingsphere.dao.UserMapper;
import com.zt.shardingsphere.entity.Order;
import com.zt.shardingsphere.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/25 22:18
 */
@SpringBootTest
public class VerticalDatabaseTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Test
    public void testInsert() {
        User user = new User();
        user.setUname("张三");
        userMapper.insert(user);
        Order order = new Order();
        order.setUserId(2L);
        order.setOrderNo("33");
        order.setAmount(BigDecimal.ZERO);
        orderMapper.insert(order);
    }


    @Test
    public void testQuery() {
        User user = userMapper.selectById(2);
        System.out.println("user = " + user);
        Order order = orderMapper.selectById(2);
        System.out.println("order = " + order);
    }
}
