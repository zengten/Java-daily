package com.zt.config;

import cn.hutool.core.util.ObjectUtil;
import com.zt.annotation.NoLogin;
import com.zt.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 免登录接口
        if(handler instanceof HandlerMethod) {
            NoLogin noLogin = ((HandlerMethod) handler).getMethodAnnotation(NoLogin.class);
            if(ObjectUtil.isNotNull(noLogin)) {
                log.debug(">>>>>免登录接口, uri = {}", request.getRequestURI());
                return true;
            }
        }
        if(UserHolder.getUser() == null) {
            response.setStatus(401);
            log.error(">>>>>login invalid, uri = {}", request.getRequestURI());
            return false;
        }
        return true;
    }

}
