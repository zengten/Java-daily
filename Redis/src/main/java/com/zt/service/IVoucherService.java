package com.zt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zt.dto.Result;
import com.zt.entity.Voucher;

/**
 * @author ZT
 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
