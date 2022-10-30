package com.zt.shardingsphere.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.shardingsphere.dao.UserMapper;
import com.zt.shardingsphere.entity.User;
import org.springframework.stereotype.Service;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/21 21:28
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
