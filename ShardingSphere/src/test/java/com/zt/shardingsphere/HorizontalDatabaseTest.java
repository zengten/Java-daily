package com.zt.shardingsphere;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zt.shardingsphere.dao.DictMapper;
import com.zt.shardingsphere.dao.OrderItemMapper;
import com.zt.shardingsphere.dao.OrderMapper;
import com.zt.shardingsphere.dao.UserMapper;
import com.zt.shardingsphere.entity.Dict;
import com.zt.shardingsphere.entity.Order;
import com.zt.shardingsphere.entity.OrderItem;
import com.zt.shardingsphere.entity.OrderVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ZT
 * @version 1.0
 * @description: 水平分库测试
 * @date 2022/10/26 20:55
 */
@SpringBootTest
public class HorizontalDatabaseTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private DictMapper dictMapper;

    /**
     * 测试 数据库分片
     */
    @Test
    public void testInsertWithDatabaseStrategy() {
        for (int i = 5; i < 10; i++) {
            Order order = new Order();
            order.setUserId((long) i);
            order.setOrderNo("1");
            order.setAmount(BigDecimal.ZERO);
            orderMapper.insert(order);
        }
    }


    /**
     * 测试 同个库多个表分片
     */
    @Test
    public void testInsertWithTableStrategy() {
        for (int i = 31; i < 40; i++) {
            Order order = new Order();
            order.setUserId((long) i);
            order.setOrderNo("test shardingSphere" + (i + 1));
            order.setAmount(BigDecimal.ZERO);
            orderMapper.insert(order);
        }
    }


    /**
     * 查询分片所有记录，会union all当前库，然后组合返回
     */
    @Test
    public void testQueryAll() {
        List<Order> orders = orderMapper.selectList(null);
        System.out.println("orders = " + orders);
        System.out.println("size = " + orders.size());
    }

    /**
     * 查询 userId > 20 数据
     */
    @Test
    public void testQueryWithUser() {
        List<Order> orders = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .gt(Order::getUserId, 20L));
        System.out.println("orders = " + orders);
        System.out.println("size = " + orders.size());
    }

    /**
     * 查询 userId 为 10 数据
     */
    @Test
    public void testQueryWithSingleData() {
        List<Order> orders = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getUserId, 10L));
        System.out.println("orders = " + orders);
        System.out.println("size = " + orders.size());
    }

    /**
     * 查询数据分页 跟之前一样 FIXME 问题：数据很大会不会很慢，因为表之间union all
     */
    @Test
    public void testQueryByPage() {
        Page<Order> page = new Page<>();
        page.setCurrent(1);
        page.setSize(5);
        Page<Order> orderPage = orderMapper.selectPage(page, Wrappers.<Order>lambdaQuery().orderByDesc(Order::getUserId));
        System.out.println("orderPage = " + orderPage.getRecords());
    }


    /**
     * 测试 同时插入多张表
     */
    @Test
    public void testInsertMultiTable() {
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setUserId(1L);
            order.setOrderNo("test" + i);
            orderMapper.insert(order);
            for (int j = 0; j < 3; j++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo("test" + i);
                orderItem.setUserId(1L);
                orderItem.setPrice(new BigDecimal(10));
                orderItem.setCount(2);
                orderItemMapper.insert(orderItem);
            }
        }

        for (int i = 5; i < 10; i++) {
            Order order = new Order();
            order.setUserId(2L);
            order.setOrderNo("test2" + i);
            orderMapper.insert(order);
            for (int j = 0; j < 3; j++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo("test2" + i);
                orderItem.setUserId(2L);
                orderItem.setPrice(new BigDecimal(20));
                orderItem.setCount(3);
                orderItemMapper.insert(orderItem);
            }
        }
    }


    /**
     * 测试联表查询
     */
    @Test
    public void testTableJoin1() {
        List<OrderVo> orderAmount = orderMapper.getOrderAmount();
        System.out.println("orderAmount = " + orderAmount);
    }

    @Test
    public void testTableJoin2() {
        List<OrderVo> orderAmount = orderItemMapper.getOrderAmount();
        System.out.println("orderAmount = " + orderAmount);
    }


    /**
     * 测试插入广播表，会插入所有分片库表中，并且数据完全一致
     */
    @Test
    public void testInsertDict() {
        Dict dict = new Dict();
        dict.setDictType("testType01");
        int insert = dictMapper.insert(dict);
    }


    /**
     * 测试查询广播表，只会查询单个库，随机一个数据库分片
     */
    @Test
    public void testQueryDict() {
        List<Dict> dicts = dictMapper.selectList(null);
        System.out.println("dicts = " + dicts);
        System.out.println("dicts.size = " + dicts.size());
    }
}
