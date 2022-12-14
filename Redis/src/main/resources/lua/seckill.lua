---秒杀脚本

-- 应该传入订单id，优惠券id，用户id

local orderId = ARGV[1]

local voucherId = ARGV[2]

local userId = ARGV[3]

-- 拼接业务前缀

local orderKey = 'seckill:order:' .. orderId

local stockKey = 'seckill:stock:' .. voucherId

-- 库存不够，返回异常码 1 (get命令返回的是字符串)
if(tonumber(redis.call('get', stockKey)) <= 0) then
    return 1
end

-- 已经购买过，返回异常码 2，不能重复下单同一优惠券，(采用set数据结构)
if(redis.call('sismember', orderKey, userId) == 1) then
    return 2
end

-- 扣减库存，(使用incrby命令，并指定值-1，另外incr命令是自增)
redis.call('incrby', stockKey, -1)

-- 下单时，保存用户到set中

redis.call('sadd', orderKey, userId)

-- 发送redis stream消息创建订单

redis.call('xadd', 'order.streams', '*', 'id', orderId, 'voucherId', voucherId, 'userId', userId)

return 0