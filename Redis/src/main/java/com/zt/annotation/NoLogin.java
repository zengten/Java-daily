package com.zt.annotation;

import java.lang.annotation.*;

/**
 * 标记不需要登录的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoLogin {
}
