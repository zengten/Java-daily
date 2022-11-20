package com.zt.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.LoginFormDTO;
import com.zt.dto.Result;
import com.zt.entity.User;
import com.zt.mapper.UserMapper;
import com.zt.service.IUserService;
import com.zt.utils.RedisConstants;
import com.zt.utils.RedisUtils;
import com.zt.utils.RequestUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private RedisUtils redisUtils;

    @Override
    public Result sendCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        redisUtils.set(RedisConstants.LOGIN_CODE_KEY + phone, code, 120);
        return Result.ok();
    }

    @Override
    public Result loginByPassword(LoginFormDTO loginForm) {
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, loginForm.getPhone()));
        if(Objects.isNull(user)) {
            return Result.fail("账号未注册！");
        }
        if(!DigestUtil.bcryptCheck(loginForm.getPassword(), user.getPassword())) {
            return Result.fail("账号或密码错误！");
        }
        String token = IdUtil.fastSimpleUUID();
        redisUtils.set(RedisConstants.LOGIN_USER_KEY + token, user, 36000);
        return Result.ok(token);
    }

    @Override
    public Result loginByMobile(LoginFormDTO loginForm) {
        String code = redisUtils.get(RedisConstants.LOGIN_CODE_KEY + loginForm.getPhone());
        if(StrUtil.isBlank(code) || !code.equals(loginForm.getCode())) {
            return Result.fail("验证码有误！");
        }
        User user = getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, loginForm.getPhone()));
        if(Objects.isNull(user)) {
            user = User.builder()
                    .phone(loginForm.getPhone())
                    .nickName("user_" + IdUtil.fastSimpleUUID().substring(0, 6))
                    .password(DigestUtil.bcrypt("Aa112233"))
                    .build();
            save(user);
        }
        String token = IdUtil.fastSimpleUUID();
        redisUtils.set(RedisConstants.LOGIN_USER_KEY + token, user, 36000);
        return Result.ok(token);
    }

    @Override
    public Result logout() {
        String key = RequestUtil.getRequest().getHeader("authorization");
        if(StrUtil.isBlank(key)) {
            return Result.fail("未登录！");
        }
        redisUtils.delete(RedisConstants.LOGIN_USER_KEY + key);
        return Result.ok();
    }
}
