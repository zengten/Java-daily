package com.zt.shardingsphere.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zt.shardingsphere.entity.Order;
import com.zt.shardingsphere.entity.OrderVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/22 16:54
 */
public interface OrderMapper extends BaseMapper<Order> {

    @Select({"SELECT o.order_no, SUM(i.price * i.count) AS amount",
            "FROM t_order o JOIN t_order_item i ON o.order_no = i.order_no",
            "GROUP BY o.order_no"})
    List<OrderVo> getOrderAmount();
}
