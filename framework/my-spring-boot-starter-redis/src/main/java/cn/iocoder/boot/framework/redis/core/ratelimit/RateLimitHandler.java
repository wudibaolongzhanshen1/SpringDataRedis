package cn.iocoder.boot.framework.redis.core.ratelimit;

public interface RateLimitHandler {

    void execute(Long voucherId, Long userId, RateLimitScene scene);
}