package com.zt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.entity.UserInfo;
import com.zt.mapper.UserInfoMapper;
import com.zt.service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * @author ZT
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
