package cn.iocoder.boot.hmdianping.mq.producer;

import cn.iocoder.boot.hmdianping.enums.rocketmq.VoucherOrderMqConstants;
import cn.iocoder.boot.hmdianping.mq.message.VoucherOrderMessage;
import cn.iocoder.boot.hmdianping.mq.template.SeckillRocketMQTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherOrderProducer {

    private final SeckillRocketMQTemplate seckillRocketMQTemplate;

    /**
     * 发送秒杀下单事务消息
     */
    public TransactionSendResult sendSeckillOrderTransaction(Long orderId, Long voucherId, Long loginUserId) {
        log.info("VoucherOrderProducer.sendSeckillOrderTransaction被执行啦，orderId：{}, " +
                "voucherId：{}，loginUserId：{}", orderId, voucherId, loginUserId);
        VoucherOrderMessage message = VoucherOrderMessage.builder()
                .orderId(orderId).voucherId(voucherId).userId(loginUserId).build();
        // 封装发送细节，Service 层不再关心 Topic 和 MessageBuilder
        return seckillRocketMQTemplate.sendMessageInTransaction(
                VoucherOrderMqConstants.TOPIC_SECKILL,
                MessageBuilder.withPayload(message).build(),
                null // 这里可以传额外的参数给监听器
        );
    }
}