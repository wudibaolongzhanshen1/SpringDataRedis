package cn.iocoder.boot.hmdianping.mq.template;

import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

// 这个注解会自动在 Spring 容器里注册一个名为 "seckillRocketMQTemplate" 的 Bean
@ExtRocketMQTemplateConfiguration(
        group = "GID_SECKILL_PRODUCER", // 独立的生产组
        nameServer = "${rocketmq.name-server}" // 复用全局的 NameServer 地址
)
public class SeckillRocketMQTemplate extends RocketMQTemplate {
    // 类体通常为空即可
}