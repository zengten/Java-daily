package com.zt.service;

/**
 * Created by Administrator.
 */
public interface AccountInfoService {

    /**
     * 李四增加金额
     * @param accountNo
     * @param amount
     */
    String updateAccountBalance(String accountNo, Double amount);
}
