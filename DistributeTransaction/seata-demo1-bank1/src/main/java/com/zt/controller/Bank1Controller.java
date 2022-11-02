package com.zt.controller;

import com.zt.service.AccountInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@RequestMapping("bank1")
public class Bank1Controller {

    @Autowired
    private AccountInfoService accountInfoService;

    /**
     * 张三转账
     * @param amount
     * @return
     */
    @GetMapping("/transfer")
    public String transfer(Double amount){
        if(Objects.isNull(amount)) {
            return "amount not be null";
        }
        accountInfoService.updateAccountBalance("1",amount);
        return "success";
    }
}
