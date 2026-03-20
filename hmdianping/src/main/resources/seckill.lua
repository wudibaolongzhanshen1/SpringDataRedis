local voucherId = KEYS[1]
local userId = ARGV[1]

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 1. 存在性判定：判断 Key 是否存在
if (redis.call('exists', stockKey) == 0) then
    return 3 -- 状态 3：优惠券信息不存在/未预热
end

-- 2. 库存判定
local stock = redis.call('get', stockKey)
if (tonumber(stock) <= 0) then
    return 1 -- 状态 1：库存不足
end

-- 3. 重复下单判定
if (redis.call('sismember', orderKey, userId) == 1) then
    return 2 -- 状态 2：已经购买过了
end

-- 4. 执行扣减
redis.call('incrby', stockKey, -1)
redis.call('sadd', orderKey, userId)
return 0 -- 状态 0：成功