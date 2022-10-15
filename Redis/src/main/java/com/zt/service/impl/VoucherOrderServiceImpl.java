package com.zt.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.Result;
import com.zt.entity.SeckillVoucher;
import com.zt.entity.VoucherOrder;
import com.zt.mapper.VoucherOrderMapper;
import com.zt.service.ISeckillVoucherService;
import com.zt.service.IVoucherOrderService;
import com.zt.utils.RedisIdGenerator;
import com.zt.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;


/**
 * @author ZT
 */
@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdGenerator redisIdGenerator;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result seckill(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(seckillVoucher.getBeginTime())) {
            return Result.fail("活动未开始！");
        }
        if(now.isAfter(seckillVoucher.getEndTime())) {
            return Result.fail("活动已经结束！");
        }
        Integer stock = seckillVoucher.getStock();
        if(stock < 1) {
            return Result.fail("库存不足！");
        }

        Long userId = UserHolder.getUser().getId();
        // 使用userId 作为锁   字符串必须加 intern() 才是同一对象
        synchronized (userId.toString().intern()) {
            // 必须使用代理对象调用方法才能使用声明式事务
            IVoucherOrderService proxy  = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        }
    }

    /**
     * class内部  内部调用方法  声明式事务失效  使用代理对象调用
     */
    @Transactional(rollbackFor = Throwable.class)
    @Override
    public Result createVoucherOrder(Long voucherId) {
        // 一人一单
        Long userId = UserHolder.getUser().getId();
        Long orderId = null;
        int count = count(Wrappers.<VoucherOrder>lambdaQuery()
                .eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getVoucherId, voucherId));
        if(count > 0) {
            return Result.fail("用户已经下过单！");
        }
        // 乐观锁实现方式一  数据库行锁1
//         boolean update = updateStockByStockVersion(voucherId, stock);

        // 乐观锁实现方式二  数据库行锁2
        boolean update = updateStockDecrement(voucherId);
        if (!update) {
            return Result.fail("系统繁忙，请稍后再试！");
        }
        orderId = redisIdGenerator.nextId("order:");
        save(VoucherOrder.builder()
                .id(orderId)
                .userId(userId)
                .voucherId(voucherId)
                .build());
        return Result.ok(orderId);
    }

    /**
     * sql: update table set stock = stock - 1 where id = ? and stock > 0
     * 注意stock值不能从java代码中传入   此时的stock可能是过期的
     */
    private boolean updateStockDecrement(Long voucherId) {
        return seckillVoucherService.lambdaUpdate()
                    .setSql("stock = stock - 1")
                    .eq(SeckillVoucher::getVoucherId, voucherId)
                    .gt(SeckillVoucher::getStock, 0)
                    .update();
    }

    /**
     * 解决方式一：乐观锁，用stock看作版本号
     */
    private boolean updateStockByStockVersion(Long voucherId, Integer stock) {
        return seckillVoucherService.update(Wrappers.<SeckillVoucher>lambdaUpdate()
                    .set(SeckillVoucher::getStock, stock - 1)
                    .eq(SeckillVoucher::getVoucherId, voucherId)
                    .eq(SeckillVoucher::getStock, stock)
                );
    }
}
