package cn.iocoder.boot.framework.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@EnableCaching
@EnableConfigurationProperties(MyCaffeineCacheProperties.class)
public class MyCaffeineCacheAutoConfiguration {

    @Resource
    private MyCaffeineCacheProperties caffeineCacheProperties;

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 检查配置是否为空
        if (caffeineCacheProperties.getConfigs() != null) {
            caffeineCacheProperties.getConfigs().forEach((name, spec) -> {
                // 构造每一个业务独立的 Caffeine 实例
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                        Caffeine.newBuilder()
                                .expireAfterWrite(spec.getTtl(), TimeUnit.SECONDS)
                                .maximumSize(spec.getMaxSize())
                                .recordStats() // 开启统计，便于后续监控
                                .build();
                // 核心步骤：将配置好的实例注册到管理器中
                cacheManager.registerCustomCache(name, nativeCache);
            });
        }
        return cacheManager;
    }
}
