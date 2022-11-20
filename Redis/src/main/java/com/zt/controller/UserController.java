package com.zt.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.zt.annotation.NoLogin;
import com.zt.dto.LoginFormDTO;
import com.zt.dto.Result;
import com.zt.dto.UserDTO;
import com.zt.entity.User;
import com.zt.entity.UserInfo;
import com.zt.service.IUserInfoService;
import com.zt.service.IUserService;
import com.zt.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author ZT
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    @NoLogin
    public Result sendCode(@RequestParam("phone") String phone) {
        if (!Validator.isMobile(phone)) {
            return Result.fail("请输入正确的手机号码！");
        }
        return userService.sendCode(phone);
    }

    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    @NoLogin
    public Result login(@RequestBody LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        if (!Validator.isMobile(phone)) {
            return Result.fail("手机号码有误！");
        }
        String password = loginForm.getPassword();
        if (StrUtil.isNotBlank(password)) {
            return userService.loginByPassword(loginForm);
        }
        return userService.loginByMobile(loginForm);
    }

    /**
     * 登出功能
     *
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout() {
        return userService.logout();
    }

    @GetMapping("/me")
    public Result me() {
        return Result.ok(UserHolder.getUser());
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    /**
     * 根据id查询用户
     * @param userId
     * @return
     */
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

}
