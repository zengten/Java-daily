package com.zt.shardingsphere.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/21 21:26
 */
@TableName("t_user")
@Data
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String uname;
}
