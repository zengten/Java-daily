package com.zt.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.zt.dao.AccountInfoDao;
import com.zt.service.AccountInfoService;
import com.zt.spring.Bank2Client;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
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

    @Autowired
    private Bank2Client bank2Client;

    /**
     * 开启全局事务
     * @param accountNo
     * @param amount
     */
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(timeoutMills = 30000)
    @Override
    public void updateAccountBalance(String accountNo, Double amount) {
        // XID 组成 机器ip + 端口号 + id
        log.info("bank1 service begin,XID：{}", RootContext.getXID());
        //扣减张三的金额
        accountInfoDao.updateAccountBalance(accountNo,amount * -1);
        //调用李四微服务，转账
        String transfer = bank2Client.transfer(amount);
        if("fail".equals(transfer)){
            // 假设fail代表网络异常，如果没有@GlobalTransactional，bank2事务不回滚，bank1事务回滚
            throw new RuntimeException("调用李四微服务异常");
        }
        ThreadUtil.sleep(30000);
    }
}
