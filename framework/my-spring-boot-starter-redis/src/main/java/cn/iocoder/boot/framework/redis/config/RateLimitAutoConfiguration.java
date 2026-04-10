package cn.iocoder.boot.framework.redis.config;

import cn.iocoder.boot.framework.redis.core.listener.NoOpRateLimitEventListener;
import cn.iocoder.boot.framework.redis.core.listener.RateLimitEventListener;
import cn.iocoder.boot.framework.redis.core.lua.TokenBucketRateLimitOperate;
import cn.iocoder.boot.framework.redis.core.ratelimit.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;


@AutoConfiguration
@EnableConfigurationProperties(SeckillRateLimitConfigProperties.class)
public class RateLimitAutoConfiguration {

    @Bean
    public TokenBucketRateLimitOperate tokenBucketRateLimitOperate(StringRedisTemplate stringRedisTemplate) {
        return new TokenBucketRateLimitOperate(stringRedisTemplate);
    }

    @Bean
    public RateLimitEventListener rateLimitEventListener() {
        return new NoOpRateLimitEventListener();
    }

    @Bean
    public RateLimitPenaltyPolicy rateLimitPenaltyPolicy(SeckillRateLimitConfigProperties seckillRateLimitConfigProperties,
                                                         StringRedisTemplate stringRedisTemplate) {

        Boolean enable = seckillRateLimitConfigProperties.getEnablePenalty();
        if (Boolean.TRUE.equals(enable)) {
            return new ThresholdPenaltyPolicy(stringRedisTemplate, seckillRateLimitConfigProperties);
        }
        return new NoOpRateLimitPenaltyPolicy();
    }

    @Bean
    public RedisRateLimitHandler redisRateLimitHandler(SeckillRateLimitConfigProperties seckillRateLimitConfigProperties,
                                                       StringRedisTemplate stringRedisTemplate,
                                                       TokenBucketRateLimitOperate tokenBucketRateLimitOperate,
                                                       RateLimitEventListener rateLimitEventListener,
                                                       RateLimitPenaltyPolicy rateLimitPenaltyPolicy) {
        return new RedisRateLimitHandler(
                stringRedisTemplate,
                seckillRateLimitConfigProperties,
                tokenBucketRateLimitOperate,
                rateLimitEventListener,
                rateLimitPenaltyPolicy
        );
    }
}
