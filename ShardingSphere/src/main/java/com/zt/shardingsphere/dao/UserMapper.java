package com.zt.shardingsphere.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zt.shardingsphere.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ZT
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
