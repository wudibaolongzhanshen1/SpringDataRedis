package cn.iocoder.boot.hmdianping.mq.consumer;

import cn.iocoder.boot.hmdianping.dal.dataobject.voucher.VoucherOrderDO;
import cn.iocoder.boot.hmdianping.enums.rocketmq.VoucherOrderMqConstants;
import cn.iocoder.boot.hmdianping.mq.message.VoucherOrderMessage;
import cn.iocoder.boot.hmdianping.service.voucher.VoucherOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = VoucherOrderMqConstants.TOPIC_SECKILL, consumerGroup = VoucherOrderMqConstants.GROUP_CONSUMER)
@RequiredArgsConstructor
@Slf4j
public class VoucherOrderConsumer implements RocketMQListener<VoucherOrderMessage> {

    private final VoucherOrderService voucherOrderService;

    @Override
    public void onMessage(VoucherOrderMessage message) {
        log.info("[RocketMQ] 接收到秒杀下单请求: {}", message);
        // 构造订单对象
        VoucherOrderDO order = new VoucherOrderDO();
        order.setId(message.getOrderId());
        order.setUserId(message.getUserId());
        order.setVoucherId(message.getVoucherId());
        try {
            // 异步创建订单（内部应包含原子性写入数据库的逻辑）
            // 注意：数据库层必须对 userId + voucherId 建立唯一索引，防止重复消费
            voucherOrderService.save(order);
        } catch (Exception e) {
            log.error("[RocketMQ] 消费下单消息失败", e);
            // 抛出异常触发重试
            throw new RuntimeException(e);
        }
    }
}
