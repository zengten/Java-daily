package com.zt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zt.dto.LoginFormDTO;
import com.zt.dto.Result;
import com.zt.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone);

    Result loginByPassword(LoginFormDTO loginForm);

    Result loginByMobile(LoginFormDTO loginForm);

    Result logout();

}
