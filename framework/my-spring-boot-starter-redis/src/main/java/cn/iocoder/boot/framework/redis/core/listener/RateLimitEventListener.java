package cn.iocoder.boot.framework.redis.core.listener;

import cn.iocoder.boot.framework.redis.core.ratelimit.RateLimitContext;

public interface RateLimitEventListener {

    /**
     * 脚本执行前回调（已计算出keys与参数）
     */
    void onBeforeExecute(RateLimitContext ctx);

    /**
     * 允许通过时回调
     */
    void onAllowed(RateLimitContext ctx);

    /**
     * 命中限流阻断时回调（区分 IP / 用户）
     */
    void onBlocked(RateLimitContext ctx, String reason);
}