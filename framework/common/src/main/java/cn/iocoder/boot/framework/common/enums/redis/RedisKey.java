package cn.iocoder.boot.framework.common.enums.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum RedisKey {
    /** 登录验证码 */
    LOGIN_CODE("login:code:", 2L, TimeUnit.MINUTES),
    /** 登录用户 Token */
    LOGIN_USER("login:token:", 30L, TimeUnit.MINUTES),
    /** 店铺信息缓存 */
    CACHE_SHOP("cache:shop:", 30L, TimeUnit.MINUTES);

    private final String prefix;
    private final Long ttl;
    private final TimeUnit unit;

    /**
     * 辅助方法：拼装完整的 Key
     */
    public String join(Object suffix) {
        return this.prefix + suffix.toString();
    }
}
