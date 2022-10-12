package com.zt.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zt.dto.UserDTO;
import com.zt.utils.RedisUtils;
import com.zt.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.zt.utils.RedisConstants.LOGIN_USER_KEY;


public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final RedisUtils redisUtils;

    public RefreshTokenInterceptor(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        // 2.基于TOKEN获取redis中的用户
        String key  = LOGIN_USER_KEY + token;
        // 3.判断用户是否存在
        String userData = redisUtils.get(key);
        if (StrUtil.isBlank(userData)) {
            return true;
        }
        UserDTO userDTO = JSONUtil.toBean(userData, UserDTO.class);
        // 6.存在，保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO);
        // 7.刷新token有效期
        redisUtils.set(key, userDTO, 3600);
        // 8.放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
