package cn.iocoder.boot.framework.redis.monitor.annotation;

import java.lang.annotation.*;

/**
 * 缓存监控注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheMonitor {

    /**
     * 缓存名称
     */
    String value() default "";

    /**
     * 是否记录命中率
     */
    boolean recordHitRate() default true;

    /**
     * 是否记录执行时间
     */
    boolean recordDuration() default true;

    /**
     * 慢查询阈值（毫秒）
     */
    long slowThreshold() default 100L;
}