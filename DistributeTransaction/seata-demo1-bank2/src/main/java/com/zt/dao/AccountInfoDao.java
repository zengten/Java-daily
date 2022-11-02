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
     * 更新账户
     * @param accountNo
     * @param amount
     * @return
     */
    @Update("UPDATE account_info SET account_balance = account_balance + #{amount} WHERE account_no = #{accountNo}")
    int updateAccountBalance(@Param("accountNo") String accountNo, @Param("amount") Double amount);

}
