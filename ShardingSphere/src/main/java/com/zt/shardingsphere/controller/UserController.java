package com.zt.shardingsphere.controller;

import com.zt.shardingsphere.service.IUserService;
import com.zt.shardingsphere.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2022/10/21 21:52
 */
@RestController
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        log.info(">>>>>entry");
        return Result.ok(userService.list());
    }

}
