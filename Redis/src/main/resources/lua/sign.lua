-- 签到脚本：判断当天是否签到，未签到时进行签到，当天签到key自增
-- getbit test 12
if(redis.call('getbit', KEYS[1], ARGV[1]) == 1) then
    -- 无需签到
    return -1
end

-- setbit test 12 1
redis.call('setbit', KEYS[1], ARGV[1], ARGV[2])

-- 签到成功，当天签到人数自增
return redis.call('incr', KEYS[2])

