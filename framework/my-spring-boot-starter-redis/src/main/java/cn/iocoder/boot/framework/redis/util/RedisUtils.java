package cn.iocoder.boot.framework.redis.util;

import cn.iocoder.boot.framework.common.exception.RedisException;
import cn.iocoder.boot.framework.common.exception.enums.RedisErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis操作工具类，统一异常处理
 */
@Slf4j
@Component
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtils(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 安全的Redis操作，带重试机制
     */
    @Retryable(value = {
            org.springframework.data.redis.RedisConnectionFailureException.class,
            org.springframework.dao.QueryTimeoutException.class
    }, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 1.5))
    public <T> T executeWithRetry(RedisOperation<T> operation) {
        try {
            return operation.execute();
        } catch (Exception e) {
            log.error("Redis操作失败，将进行重试", e);
            throw new RedisException("Redis操作失败", e);
        }
    }

    /**
     * 安全的设置缓存值
     */
    public void setWithRetry(String key, String value, long timeout, TimeUnit unit) {
        executeWithRetry(() -> {
            stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
            return null;
        });
    }

    /**
     * 安全的获取缓存值
     */
    public String getWithRetry(String key) {
        return executeWithRetry(() -> stringRedisTemplate.opsForValue().get(key));
    }

    /**
     * 安全的删除缓存
     */
    public Boolean deleteWithRetry(String key) {
        return executeWithRetry(() -> stringRedisTemplate.delete(key));
    }

    /**
     * 安全的设置过期时间
     */
    public Boolean expireWithRetry(String key, long timeout, TimeUnit unit) {
        return executeWithRetry(() -> stringRedisTemplate.expire(key, timeout, unit));
    }

    /**
     * 安全的检查key是否存在
     */
    public Boolean hasKeyWithRetry(String key) {
        return executeWithRetry(() -> stringRedisTemplate.hasKey(key));
    }

    /**
     * 带异常处理的缓存查询模板
     */
    public <T> T cacheTemplate(String key, Supplier<T> cacheSupplier, Supplier<T> dbSupplier,
                              long timeout, TimeUnit unit) {
        try {
            // 先查缓存
            String cachedValue = getWithRetry(key);
            if (cachedValue != null) {
                return cacheSupplier.get();
            }

            // 查数据库
            T result = dbSupplier.get();
            if (result != null) {
                // 写入缓存
                setWithRetry(key, result.toString(), timeout, unit);
            }
            return result;
        } catch (RedisException e) {
            log.error("缓存操作失败，降级到数据库查询", e);
            // 降级到数据库查询
            return dbSupplier.get();
        }
    }

    /**
     * Redis操作接口
     */
    @FunctionalInterface
    public interface RedisOperation<T> {
        T execute();
    }

    /**
     * Redis操作结果包装
     */
    public static class RedisResult<T> {
        private final boolean success;
        private final T data;
        private final String errorMessage;

        private RedisResult(boolean success, T data, String errorMessage) {
            this.success = success;
            this.data = data;
            this.errorMessage = errorMessage;
        }

        public static <T> RedisResult<T> success(T data) {
            return new RedisResult<>(true, data, null);
        }

        public static <T> RedisResult<T> failure(String errorMessage) {
            return new RedisResult<>(false, null, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public T getData() {
            return data;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public T getDataOrThrow() {
            if (!success) {
                throw new RedisException(RedisErrorCodeConstants.REDIS_COMMAND_FAILED.getCode(), errorMessage);
            }
            return data;
        }
    }
}