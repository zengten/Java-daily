package com.zt.shardingsphere.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author ZT
 * @version 1.0
 * @description: 广播表  作用：关联查询时，避免跨库
 * @date 2022/10/27 22:17
 */
@TableName("t_dict")
@Data
public class Dict {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String dictType;
}
