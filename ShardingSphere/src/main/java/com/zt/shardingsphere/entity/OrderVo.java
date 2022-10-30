package com.zt.shardingsphere.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/27 21:28
 */
@Data
@AllArgsConstructor
public class OrderVo {

    private String orderNo;

    private BigDecimal amount;
}
