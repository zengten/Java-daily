package com.zt.config;

import com.zt.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RefreshTokenInterceptor(redisUtils)).addPathPatterns("/**").order(0);
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns("/user/login", "/user/code", "/shop/**")
                .order(1);
    }
}
