package com.zt.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zt.entity.AccountInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * Created by Administrator.
 * @author ZT
 */
public interface AccountInfoDao extends BaseMapper<AccountInfo> {

    /**
     * 更新账户金额
     * @param accountNo
     * @param amount
     * @return
     */
    @Update("update account_info set account_balance = account_balance + #{amount} where account_no = #{accountNo}")
    int updateAccountBalance(@Param("accountNo") String accountNo, @Param("amount") Double amount);

}
