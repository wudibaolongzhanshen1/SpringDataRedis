package cn.iocoder.boot.framework.redis.config;

import cn.iocoder.boot.framework.redis.core.CacheClient;
import cn.iocoder.boot.framework.redis.core.cache.CacheConfig;
import cn.iocoder.boot.framework.redis.core.cache.EnhancedCacheClient;
import cn.iocoder.boot.framework.redis.core.listener.RateLimitEventListener;
import cn.iocoder.boot.framework.redis.core.lua.TokenBucketRateLimitOperate;
import cn.iocoder.boot.framework.redis.core.ratelimit.RateLimitPenaltyPolicy;
import cn.iocoder.boot.framework.redis.core.ratelimit.RedisRateLimitHandler;
import cn.iocoder.boot.framework.redis.core.ratelimit.SeckillRateLimitConfigProperties;
import cn.iocoder.boot.framework.redis.core.ratelimit.ThresholdPenaltyPolicy;
import cn.iocoder.boot.framework.redis.monitor.RedisHealthIndicator;
import cn.iocoder.boot.framework.redis.monitor.RedisMonitor;
import cn.iocoder.boot.framework.redis.monitor.RedisMonitorConfig;
import cn.iocoder.boot.framework.redis.template.RedisOperationTemplate;
import cn.iocoder.boot.framework.redis.util.RedisUtils;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Redis自动配置
 */
@AutoConfiguration
@EnableRetry
@EnableScheduling
@EnableConfigurationProperties({CacheConfig.class, RedisMonitorConfig.class})
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheClient cacheClient(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        return new CacheClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public EnhancedCacheClient enhancedCacheClient(StringRedisTemplate stringRedisTemplate,
                                                   RedissonClient redissonClient,
                                                   CacheConfig cacheConfig) {
        return new EnhancedCacheClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisUtils redisUtils(StringRedisTemplate stringRedisTemplate) {
        return new RedisUtils(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisOperationTemplate redisOperationTemplate(StringRedisTemplate stringRedisTemplate,
                                                         RedisUtils redisUtils) {
        return new RedisOperationTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisRateLimitHandler redisRateLimitHandler(StringRedisTemplate stringRedisTemplate, SeckillRateLimitConfigProperties seckillRateLimitConfigProperties, TokenBucketRateLimitOperate tokenBucketRateLimitOperate, RateLimitEventListener rateLimitEventListener, RateLimitPenaltyPolicy rateLimitPenaltyPolicy) {
        return new RedisRateLimitHandler(stringRedisTemplate, seckillRateLimitConfigProperties, tokenBucketRateLimitOperate, rateLimitEventListener, rateLimitPenaltyPolicy);
    }

    @Bean
    @ConditionalOnMissingBean
    public ThresholdPenaltyPolicy thresholdPenaltyPolicy(StringRedisTemplate stringRedisTemplate, SeckillRateLimitConfigProperties seckillRateLimitConfigProperties) {
        return new ThresholdPenaltyPolicy(stringRedisTemplate, seckillRateLimitConfigProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.redis.monitor.enabled", havingValue = "true", matchIfMissing = false)
    public RedisMonitor redisMonitor(StringRedisTemplate stringRedisTemplate,
                                     RedisMonitorConfig monitorConfig) {
        return new RedisMonitor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.redis.monitor.enabled", havingValue = "true", matchIfMissing = false)
    public RedisHealthIndicator redisHealthIndicator(StringRedisTemplate stringRedisTemplate,
                                                     RedisMonitorConfig monitorConfig) {
        return new RedisHealthIndicator(stringRedisTemplate, monitorConfig);
    }

}