package cn.iocoder.boot.framework.redis.template;

import cn.iocoder.boot.framework.common.exception.RedisException;
import cn.iocoder.boot.framework.common.exception.enums.RedisErrorCodeConstants;
import cn.iocoder.boot.framework.redis.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Redis操作模板，提供统一的执行模式
 */
@Slf4j
@Component
public class RedisOperationTemplate {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 执行Redis操作，带异常处理
     */
    public <T> T execute(RedisOperation<T> operation) {
        try {
            return operation.execute();
        } catch (Exception e) {
            log.error("Redis操作执行失败", e);
            throw new RedisException("Redis操作失败", e);
        }
    }

    /**
     * 执行Redis操作，带重试
     */
    public <T> T executeWithRetry(RedisOperation<T> operation, int maxRetries) {
        int retryCount = 0;
        while (retryCount <= maxRetries) {
            try {
                return operation.execute();
            } catch (Exception e) {
                retryCount++;
                if (retryCount > maxRetries) {
                    log.error("Redis操作重试{}次后失败", maxRetries, e);
                    throw new RedisException("Redis操作重试失败", e);
                }
                log.warn("Redis操作失败，第{}次重试", retryCount);
                try {
                    Thread.sleep(1000 * retryCount); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RedisException("重试被中断", ie);
                }
            }
        }
        throw new RedisException(RedisErrorCodeConstants.REDIS_COMMAND_FAILED);
    }

    /**
     * 缓存查询模板
     */
    public <T> T cacheQuery(String key, Supplier<T> cacheLoader,
                           Supplier<T> dbLoader, long timeout, TimeUnit unit) {
        // 先查缓存
        T cached = execute(() -> {
            String value = stringRedisTemplate.opsForValue().get(key);
            return cacheLoader.get();
        });

        if (cached != null) {
            return cached;
        }

        // 查数据库
        T dbResult = dbLoader.get();
        if (dbResult != null) {
            // 写入缓存
            execute(() -> {
                stringRedisTemplate.opsForValue().set(key, dbResult.toString(), timeout, unit);
                return null;
            });
        } else {
            // 空值缓存
            execute(() -> {
                stringRedisTemplate.opsForValue().set(key, "", Math.min(timeout, 120), TimeUnit.SECONDS);
                return null;
            });
        }

        return dbResult;
    }

    /**
     * 分布式锁模板
     */
    public <T> T withLock(String lockKey, Supplier<T> operation,
                          long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            log.info("尝试获取锁: {}", lockKey);
            // 尝试获取锁
            if (!lock.tryLock(waitTime, leaseTime, unit)) {
                throw new RedisException(RedisErrorCodeConstants.REDIS_LOCK_TIMEOUT);
            }

            try {
                return operation.get();
            } finally {
                // 纠正：只有持有锁且锁未过期时才释放
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("释放锁: {}", lockKey);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisException("获取锁被中断", e);
        }
    }

    /**
     * 管道操作模板
     */
    public void pipeline(Consumer<RedisConnection> operations) {
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // 纠正：直接对 connection 进行底层操作，或使用特定序列化器写入
            operations.accept(connection);
            return null;
        });
    }

    /**
     * 事务操作模板
     */
    public void transaction(Consumer<StringRedisTemplate> operations) {
        stringRedisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operationsInSession) {
                operationsInSession.multi(); // 开启事务
                try {
                    operations.accept((StringRedisTemplate) operationsInSession);
                    return operationsInSession.exec(); // 提交事务
                } catch (Exception e) {
                    operationsInSession.discard(); // 异常回滚
                    throw e;
                }
            }
        });
    }

    /**
     * Redis操作接口
     */
    @FunctionalInterface
    public interface RedisOperation<T> {
        T execute();
    }

    /**
     * 操作结果包装
     */
    public static class OperationResult<T> {
        private final boolean success;
        private final T data;
        private final String error;

        private OperationResult(boolean success, T data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }

        public static <T> OperationResult<T> success(T data) {
            return new OperationResult<>(true, data, null);
        }

        public static <T> OperationResult<T> failure(String error) {
            return new OperationResult<>(false, null, error);
        }

        public boolean isSuccess() {
            return success;
        }

        public T getData() {
            return data;
        }

        public String getError() {
            return error;
        }

        public T getDataOrThrow() {
            if (!success) {
                throw new RedisException(error);
            }
            return data;
        }
    }
}