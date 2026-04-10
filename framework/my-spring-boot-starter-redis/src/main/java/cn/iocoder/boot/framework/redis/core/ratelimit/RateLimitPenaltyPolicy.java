package cn.iocoder.boot.framework.redis.core.ratelimit;

public interface RateLimitPenaltyPolicy {

    void apply(RateLimitContext context, Long result);
}