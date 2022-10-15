package com.zt.config;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 接口耗时统计
 * @author ZT
 */
@Slf4j
public class DurationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        chain.doFilter(servletRequest, servletResponse);
        log.info("[Api Access] ==> uri: {}, duration: {}ms",request.getRequestURI(),System.currentTimeMillis() - start);
    }
}