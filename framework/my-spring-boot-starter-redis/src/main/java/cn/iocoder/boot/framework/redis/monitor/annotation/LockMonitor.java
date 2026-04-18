package cn.iocoder.boot.framework.redis.monitor.annotation;

import java.lang.annotation.*;

/**
 * 分布式锁监控注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LockMonitor {

    /**
     * 锁名称
     */
    String value() default "";

    /**
     * 是否记录等待时间
     */
    boolean recordWaitTime() default true;

    /**
     * 是否记录持有时间
     */
    boolean recordHoldTime() default true;

    /**
     * 超时阈值（毫秒）
     */
    long timeoutThreshold() default 5000L;
}