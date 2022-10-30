package com.zt.shardingsphere.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zt.shardingsphere.entity.OrderItem;
import com.zt.shardingsphere.entity.OrderVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/27 21:02
 */
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select({"select t1.order_no,sum(t1.price * t1.count) as amount",
            "from t_order_item t1 join t_order t2 on t1.order_no = t2.order_no\n" +
            "GROUP BY t1.order_no"})
    List<OrderVo> getOrderAmount();
}
