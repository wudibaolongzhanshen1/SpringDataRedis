package cn.iocoder.boot.hmdianping.mq.consumer;

import cn.iocoder.boot.hmdianping.canal.context.CanalHandlerContext;
import cn.iocoder.boot.hmdianping.canal.handler.EntryHandler;
import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.FlatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${canal.mq.topic:canal-test-topic}",
        consumerGroup = "canal-sync-group"
)
public class CanalMqConsumer implements RocketMQListener<String> {

    @Resource
    private CanalHandlerContext handlerContext;

    @Override
    public void onMessage(String message) {
        try {
            // 1. 将 MQ 的 JSON 字符串反序列化为 FlatMessage 对象
            // 注意：需要引入 canal.protocol 依赖
            FlatMessage flatMessage = JSON.parseObject(message, FlatMessage.class);
            if (flatMessage == null || flatMessage.getData() == null) {
                return;
            }
            // 2. 获取表名并分发给对应的策略处理器
            String tableName = flatMessage.getTable();
            EntryHandler handler = handlerContext.getHandler(tableName);
            if (handler != null) {
                handler.handle(flatMessage);
            } else {
                log.warn("数据库执行了操作：{}，但没有找到对应的 EntryHandler 处理表: {}", flatMessage.getType(), tableName);
            }
        } catch (Exception e) {
            log.error("消费 Canal 消息失败，消息内容: {}", message, e);
            // 抛出异常触发 RocketMQ 的重试机制（默认重试 16 次）
            throw new RuntimeException("消费失败，触发 MQ 重试", e);
        }
    }
}