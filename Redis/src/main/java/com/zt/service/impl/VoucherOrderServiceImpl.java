package com.zt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.Result;
import com.zt.entity.SeckillVoucher;
import com.zt.entity.VoucherOrder;
import com.zt.mapper.VoucherOrderMapper;
import com.zt.service.ISeckillVoucherService;
import com.zt.service.IVoucherOrderService;
import com.zt.utils.RedisConstants;
import com.zt.utils.RedisIdGenerator;
import com.zt.utils.RedisScriptUtil;
import com.zt.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;


/**
 * @author ZT
 */
@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    @SuppressWarnings("SpellCheckingInspection")
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdGenerator redisIdGenerator;

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Autowired
    private RedisScriptUtil redisScriptUtil;

    private final Thread handlePendingListThread;

    private final Thread handleCreateOrderThread;

    /**
     * 异步创建订单消费线程 和 出现异常情况pendingList线程
     */
    public VoucherOrderServiceImpl() {
        this.handlePendingListThread = new Thread(new SeckillPendListHandler(), "handlePendingList");
        this.handleCreateOrderThread = new Thread(new CreateOrderHandler(), "handleCreateOrder");
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public Result seckill(Long voucherId) {
//        return useLockCreateOrder(voucherId);
        return asyncCreateOrder(voucherId);
    }


    @PostConstruct
    private void threadInit() {
        // 初始化stream
        initOrderStream();
        // 休眠1s防止stream没有初始化完成
        ThreadUtil.sleep(1000);
        // 启动消息监听
        handlePendingListThread.start();
        handleCreateOrderThread.start();
    }

    /**
     * 初始化创建订单的数据key和消费分组
     */
    private void initOrderStream() {
        Boolean hasKey = stringRedisTemplate.hasKey(RedisConstants.SECKILL_STREAM);
        if(BooleanUtil.isFalse(hasKey)) {
            log.info(">>>stream key不存在，开始创建");
            stringRedisTemplate.opsForStream().createGroup(RedisConstants.SECKILL_STREAM, "g1");
        }
        StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(RedisConstants.SECKILL_STREAM);
        if(groups.isEmpty()) {
            log.info(">>>消费组不存在，开始创建");
            stringRedisTemplate.opsForStream().createGroup(RedisConstants.SECKILL_STREAM, "g1");
        }
    }

    @SuppressWarnings({"unchecked", "InfiniteLoopStatement"})
    class CreateOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // 读取创建订单消息
                    List<MapRecord<String, Object, Object>> recordList = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2L)),
                            StreamOffset.create(RedisConstants.SECKILL_STREAM, ReadOffset.lastConsumed())
                    );
                    // 无消息，继续下次循环，然后阻塞
                    if (CollectionUtil.isEmpty(recordList)) {
                        continue;
                    }
                    // 创建订单
                    useConsumerCreateOrder(recordList.get(0));
                } catch (Exception e) {
                    log.error(">>>创建订单异常，唤醒pendList线程处理", e);
                    // 进入pendingList处理
                    LockSupport.unpark(handlePendingListThread);
                }
            }
        }
    }

    @SuppressWarnings({"InfiniteLoopStatement", "unchecked", "SpellCheckingInspection"})
    class SeckillPendListHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    List<MapRecord<String, Object, Object>> recordList = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1L).block(Duration.ofSeconds(2L)),
                            StreamOffset.create(RedisConstants.SECKILL_STREAM, ReadOffset.from("0"))
                    );
                    if (CollectionUtil.isEmpty(recordList)) {
                        log.info(">>>>>pendingList无数据, 线程休眠待唤醒");
                        LockSupport.park();
                        continue;
                    }
                    // 创建订单
                    useConsumerCreateOrder(recordList.get(0));
                } catch (Exception e) {
                    log.error(">>>处理pendingList异常， ", e);
                }
            }
        }
    }

    /**
     * 使用stream创建订单
     * @param data 订单数据
     */
    private void useConsumerCreateOrder(MapRecord<String, Object, Object> data) {
        RecordId recordId = data.getId();
        Map<Object, Object> createOrderData = data.getValue();
        VoucherOrder voucherOrder = BeanUtil.toBean(createOrderData, VoucherOrder.class);
        boolean stockDecrement = updateStockDecrement(voucherOrder.getVoucherId());
        if(!stockDecrement) {
            log.error(">>>库存扣减失败！");
            return;
        }
        if (save(voucherOrder)) {
            log.info(">>>创建订单成功，id = {}", voucherOrder.getId());
            Long acknowledge = stringRedisTemplate.opsForStream().acknowledge(
                    RedisConstants.SECKILL_STREAM,
                    "g1",
                    recordId
            );
            log.info(">>>订单成功确认{}条", acknowledge);
        }
    }

    /**
     * 异步创建订单
     * 问题1：消息的实时性，后面付款问题需要
     * 问题2：扣减库存的查询实时性，前端展示(可以延迟)
     * 问题3：延迟取消订单实现
     */
    private Result asyncCreateOrder(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdGenerator.nextId(RedisConstants.SECKILL_ID_PREFIX);
        RedisScript<Long> script = redisScriptUtil.getRedisScript(RedisScriptUtil.ScriptEnum.SECKILL);
        Long res = stringRedisTemplate.execute(
                script,
                ListUtil.empty(),
                String.valueOf(orderId), voucherId.toString(), userId.toString()
        );
        // 默认显示库存不足
        int resIntValue = Convert.toInt(res, 1);
        if (resIntValue != 0) {
            return Result.fail(resIntValue == 1 ? "已经抢购一空啦！" : "已经下单过了，不能重复下单！");
        }
        return Result.ok(orderId);
    }

    /**
     * 使用锁秒杀，同步创建订单
     * synchronized,simpleRedisLock,redissonLock
     */
    private Result useLockCreateOrder(Long voucherId) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (Objects.isNull(seckillVoucher)) {
            return Result.fail("秒杀不存在！");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillVoucher.getBeginTime())) {
            return Result.fail("活动未开始！");
        }
        if (now.isAfter(seckillVoucher.getEndTime())) {
            return Result.fail("活动已经结束！");
        }
        Integer stock = seckillVoucher.getStock();
        if (stock < 1) {
            return Result.fail("库存不足！");
        }
        Long userId = UserHolder.getUser().getId();
        // 使用 synchronized 锁
//        return useSynchronizedLock(userId, voucherId);
        // 使用 SimpleRedisLock
//        SimpleRedisLock lock = new SimpleRedisLock(userId.toString(), stringRedisTemplate);
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            return Result.fail("系统繁忙，请稍后再试！");
        }
        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 使用 synchronized 锁   仅适用于 单机情况
     */
    private Result useSynchronizedLock(Long userId, Long voucherId) {
        // 使用userId 作为锁   字符串必须加 intern() 才是同一对象
        synchronized (userId.toString().intern()) {
            // 必须使用代理对象调用方法才能使用声明式事务
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
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
        int count = count(Wrappers.<VoucherOrder>lambdaQuery()
                .eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getVoucherId, voucherId));
        if (count > 0) {
            return Result.fail("用户已经下过单！");
        }
        // 乐观锁实现方式一  数据库行锁1
//         boolean update = updateStockByStockVersion(voucherId, stock);

        // 乐观锁实现方式二  数据库行锁2
        boolean update = updateStockDecrement(voucherId);
        if (!update) {
            return Result.fail("系统繁忙，请稍后再试！");
        }
        Long orderId = redisIdGenerator.nextId("order:");
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
