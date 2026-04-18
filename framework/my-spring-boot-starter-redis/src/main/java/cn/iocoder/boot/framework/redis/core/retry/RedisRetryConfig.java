package cn.iocoder.boot.framework.redis.core.retry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis重试配置
 */
@Data
@ConfigurationProperties(prefix = "spring.redis.retry")
public class RedisRetryConfig {

    /**
     * 是否启用重试机制
     */
    private boolean enabled = true;

    /**
     * 最大重试次数
     */
    private int maxAttempts = 3;

    /**
     * 重试间隔（毫秒）
     */
    private long backoffDelay = 1000;

    /**
     * 重试间隔乘数
     */
    private double backoffMultiplier = 1.5;

    /**
     * 最大重试间隔（毫秒）
     */
    private long maxBackoffDelay = 5000;

    /**
     * 需要重试的异常类型
     */
    private Class<? extends Throwable>[] retryableExceptions = new Class[]{
            org.springframework.data.redis.RedisConnectionFailureException.class,
            org.springframework.dao.QueryTimeoutException.class,
            java.net.ConnectException.class,
            java.net.SocketTimeoutException.class
    };

}