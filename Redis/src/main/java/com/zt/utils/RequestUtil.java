package com.zt.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 请求对象工具类
 */
@Slf4j
public class RequestUtil {

    /**
     * 从请求中获取参数
     * @param paramName
     * @return
     */
    public static String getParam(String paramName){
        HttpServletRequest request=getRequest();
        if (StrUtil.isEmpty(paramName)){
            return "";
        }
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables.containsKey(paramName)){
            return ObjectUtil.toString(pathVariables.get(paramName));
        }
        if (!ObjectUtil.isEmpty(request.getAttribute(paramName))){
            return ObjectUtil.toString(request.getAttribute(paramName));
        }
        return request.getParameter(paramName);
    }


    /**
     * 获取当前request
     * @Description: 获取当前request
     * @return
     * @throws IllegalStateException 当前线程不是web请求抛出此异常.
     */
    public static HttpServletRequest getRequest() throws IllegalStateException {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new RuntimeException("当前线程中不存在Request上下文");
        }
        return attrs.getRequest();
    }

    /**
     * 获取当前response
     * @Description: 获取当前response
     * @return
     * @throws IllegalStateException 当前线程不是web请求抛出此异常.
     */
    public static HttpServletResponse getResponse() throws IllegalStateException {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new RuntimeException("当前线程中不存在Request上下文");
        }
        return attrs.getResponse();
    }
}
