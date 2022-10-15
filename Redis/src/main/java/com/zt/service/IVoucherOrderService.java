package com.zt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zt.dto.Result;
import com.zt.entity.VoucherOrder;

/**
 *
 * @author ZT
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckill(Long voucherId);

    Result createVoucherOrder(Long voucherId);

}
