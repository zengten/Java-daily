package com.zt.config;

import com.zt.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error(e.toString(), e);
        return Result.fail("服务器异常");
    }
}
