package com.zt.controller;

import com.zt.service.AccountInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@RequestMapping("bank2")
public class Bank2Controller {

    @Autowired
    private AccountInfoService accountInfoService;

    /**
     * 接收张三的转账
     *
     * @param amount
     * @return
     */
    @GetMapping("/transfer")
    public String transfer(Double amount) {
        //李四增加金额
        return accountInfoService.updateAccountBalance("2", amount);
    }
}
