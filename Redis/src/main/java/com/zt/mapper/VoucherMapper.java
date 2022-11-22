package com.zt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zt.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ZT
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
