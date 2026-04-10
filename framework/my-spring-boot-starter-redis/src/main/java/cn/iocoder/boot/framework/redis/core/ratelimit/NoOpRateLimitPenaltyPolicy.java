package cn.iocoder.boot.framework.redis.core.ratelimit;


public class NoOpRateLimitPenaltyPolicy implements RateLimitPenaltyPolicy {
    @Override
    public void apply(RateLimitContext ctx, Long result) {
        // no-op
    }
}