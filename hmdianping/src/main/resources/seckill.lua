
local voucherId = KEYS[1]
local userId = ARGV[1]
-- 2.数据key
-- 2.1.库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2.订单key
local orderKey = 'seckill:order:' .. voucherId

if(tonumber(redis.call('get', stockKey)) <= 0) then
    -- 库存不足
    return 1
end

if(redis.call('sismember', orderKey, userId) == 1) then
    -- 已经购买过了
    return 2
end

redis.call('incrby', stockKey, -1)
redis.call('sadd', orderKey, userId)
return 0