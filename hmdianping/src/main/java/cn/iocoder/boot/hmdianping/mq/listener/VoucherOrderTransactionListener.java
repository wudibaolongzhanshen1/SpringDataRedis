package cn.iocoder.boot.hmdianping.mq.listener;

import cn.iocoder.boot.hmdianping.dal.redis.voucher.VoucherOrderRedisDAO;
import cn.iocoder.boot.hmdianping.mq.message.VoucherOrderMessage;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;

@RocketMQTransactionListener
@RequiredArgsConstructor
@Slf4j
public class VoucherOrderTransactionListener implements RocketMQLocalTransactionListener {

    private final VoucherOrderRedisDAO voucherOrderRedisDAO;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 执行本地事务：这里执行 Lua 脚本
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            VoucherOrderMessage payload = JSON.parseObject(new String((byte[]) msg.getPayload()), VoucherOrderMessage.class);
            // 执行 Lua 脚本：判断库存、判重、扣减并记录成功标记
            // 建议修改 Lua 脚本，在扣减成功时同时往 Redis Set 塞入 orderId 或 userId 标记
            Long result = voucherOrderRedisDAO.executeSeckillVoucherOrderLua(payload.getVoucherId(), payload.getUserId());
            if (result == 0) {
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (Exception e) {
            log.error("本地事务执行异常", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 回查机制：如果执行本地事务后没能 COMMIT，Broker 会调用此方法
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        VoucherOrderMessage payload = JSON.parseObject(new String((byte[]) msg.getPayload()), VoucherOrderMessage.class);
        // 去 Redis 查询“抢购成功名单”中是否存在该用户
        // 这要求 Lua 脚本在执行成功时，必须 sadd 进一个标记 Key
        Boolean isExisted = stringRedisTemplate.opsForSet().isMember(
                "seckill:order:" + payload.getVoucherId(),
                payload.getUserId().toString());
        return Boolean.TRUE.equals(isExisted) ?
                RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.ROLLBACK;
    }
}