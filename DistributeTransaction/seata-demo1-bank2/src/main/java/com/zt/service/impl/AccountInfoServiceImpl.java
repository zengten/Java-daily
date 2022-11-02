package com.zt.service.impl;

import com.zt.dao.AccountInfoDao;
import com.zt.service.AccountInfoService;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
@Slf4j
public class AccountInfoServiceImpl implements AccountInfoService {

    @Autowired
    private AccountInfoDao accountInfoDao;


    @Transactional
    @Override
    public String updateAccountBalance(String accountNo, Double amount) {
        // bank1未开启GlobalTransactional，XID为null
        log.info("bank2 service begin,XID：{}", RootContext.getXID());
        //李四增加金额
        accountInfoDao.updateAccountBalance(accountNo, amount);
        if (amount == 200) {
            //人为制造异常
//            throw new RuntimeException("bank2 make exception..");
            return "fail";
        }
        return "success";
    }
}
