package cn.iocoder.boot.framework.cache.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "app.cache")
@Data
public class MyCaffeineCacheProperties {

    private Map<String, CacheSpec> configs;

    @Data
    public static class CacheSpec {
        private Integer ttl;      // 过期时间，单位：秒
        private Integer maxSize;  // 最大容量
    }
}
