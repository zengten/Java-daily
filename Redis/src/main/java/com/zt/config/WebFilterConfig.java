package com.zt.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFilterConfig {

    /**
     * 请求API时间统计
     * @return
     */
    @Bean
    @SuppressWarnings("unchecked")
    public FilterRegistrationBean durationFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new DurationFilter());
        registration.addUrlPatterns("/*");
        registration.setName("durationFilter");
        registration.setOrder(2000);
        return registration;
    }
}
