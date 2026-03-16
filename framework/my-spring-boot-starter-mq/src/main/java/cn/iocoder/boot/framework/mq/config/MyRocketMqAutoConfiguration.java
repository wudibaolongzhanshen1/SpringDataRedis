package cn.iocoder.boot.framework.mq.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

@AutoConfiguration
@Slf4j
public class MyRocketMqAutoConfiguration {
    @Bean
    @Primary // 确保优先使用这个自定义转换器
    public RocketMQMessageConverter rocketMQMessageConverter(ObjectMapper objectMapper) {
        RocketMQMessageConverter converter = new RocketMQMessageConverter();
        // 核心：将自定义的 ObjectMapper 设置到转换器中
        CompositeMessageConverter compositeConverter = (CompositeMessageConverter) converter.getMessageConverter();
        // 找到 MappingJackson2MessageConverter 并替换其 ObjectMapper
        compositeConverter.getConverters().stream()
                .filter(c -> c instanceof MappingJackson2MessageConverter)
                .map(c -> (MappingJackson2MessageConverter) c)
                .forEach(jacksonConverter -> jacksonConverter.setObjectMapper(objectMapper));
        log.info("[RocketMQ] 已成功注入全局 Jackson 配置，支持 LocalDateTime 序列化");
        return converter;
    }
}
