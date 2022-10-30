package com.zt.shardingsphere.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/22 16:53
 */
@Data
@TableName("t_order")
public class Order {

    /**
     * AUTO时，依赖数据库本身生成id，相当于mybatis plus不填充策略
     * 可以配置使用shardingSphere 生成分布式id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal amount;
}
