package cn.iocoder.boot.framework.redis.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.boot.framework.common.exception.RedisException;
import cn.iocoder.boot.framework.common.exception.enums.RedisErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CacheClient {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 解决缓存击穿与穿透的通用查询模板
     *
     * @param keyPrefix  Redis 键前缀
     * @param id         查询的主键 ID
     * @param returnType 返回值的实体类型
     * @param bloomCheck 布隆过滤器的检查逻辑 (Predicate 函数)
     * @param dbFallback 数据库查询逻辑 (Function 函数)
     * @param <R>        返回值泛型
     * @param <ID>       ID 泛型
     * @return 实体对象
     */
    public <R, ID> R queryWithMutexAndBloom(
            String keyPrefix, ID id, Class<R> returnType,
            Predicate<ID> bloomCheck, Function<ID, R> dbFallback) {
        // 1. 【防穿透】执行传入的布隆检查逻辑
        if (!bloomCheck.test(id)) {
            log.info("布隆过滤器拦截：id {} 不存在", id);
            return null;
        }
        String key = keyPrefix + id;
        // 2. 一重检查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, returnType);
        }
        // 处理防穿透的空值哨兵 (假设存的是空字符串 "")
        if ("".equals(json)) {
            return null;
        }
        // 3. 准备抢锁
        String lockKey = "lock:" + keyPrefix + id;
        RLock rLock = redissonClient.getLock(lockKey);
        try {
            boolean getLock = rLock.tryLock(5, TimeUnit.SECONDS);
            if (!getLock) {
                log.warn("获取锁超时，放弃等待。lockKey={}", lockKey);
                throw new RedisException(RedisErrorCodeConstants.REDIS_LOCK_TIMEOUT);
            }
            // 4. 二重检查缓存
            json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                return JSONUtil.toBean(json, returnType);
            }
            if ("".equals(json)) {
                return null;
            }
            // 5. 【核心解耦点】执行传入的数据库查询逻辑
            R dbResult = dbFallback.apply(id);
            // 6. 回写缓存及空值兜底
            if (dbResult == null) {
                // 空值缓存，使用2分钟固定时间
                stringRedisTemplate.opsForValue().set(key, "", 2, TimeUnit.MINUTES);
            } else {
                // 正常缓存，设置30分钟过期时间
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(dbResult), 30, TimeUnit.MINUTES);
            }
            return dbResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisException("Redis操作被中断", e);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }


    public <R, ID> Set<R> querySetWithMutexAndBloom(
            String keyPrefix, ID id, Class<R> elementType,
            Predicate<ID> bloomCheck, Function<ID, Set<R>> dbFallback,
            DefaultRedisScript<Long> redisScript, Long ttl, TimeUnit unit) {
        if (!bloomCheck.test(id)) {
            return Collections.emptySet();
        }
        String key = keyPrefix + id;
        // 2. 一重检查缓存
        if (BooleanUtil.isTrue(stringRedisTemplate.hasKey(key))) {
            return getSetFromCache(key, elementType);
        }
        // 3. 准备抢锁
        String lockKey = "lock:" + key;
        RLock rLock = redissonClient.getLock(lockKey);
        try {
            boolean getLock = rLock.tryLock(5, TimeUnit.SECONDS);
            if (!getLock) {
                throw new RedisException(RedisErrorCodeConstants.REDIS_LOCK_TIMEOUT);
            }
            // 4. 二重检查缓存
            if (BooleanUtil.isTrue(stringRedisTemplate.hasKey(key))) {
                return getSetFromCache(key, elementType);
            }
            // 5. 执行数据库查询逻辑
            Set<R> dbResult = dbFallback.apply(id);
            // 6. 原子回写缓存 (仅在有数据时回写)
            if (dbResult != null && !dbResult.isEmpty()) {
                // 将对象集合序列化为 JSON 字符串集合
                List<String> args = new ArrayList<>();
                args.add(String.valueOf(unit.toSeconds(ttl))); // ARGV[1]: 过期时间（秒）
                for (R item : dbResult) {
                    if (item instanceof String) {
                        args.add((String) item);
                    } else if (item instanceof Number || item instanceof Boolean) {
                        args.add(String.valueOf(item));
                    } else {
                        args.add(JSONUtil.toJsonStr(item));
                    }
                }
                // 执行 Lua 脚本：KEYS[1] 为缓存键
                stringRedisTemplate.execute(redisScript, Collections.singletonList(key), args.toArray());
            }
            return dbResult;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisException("Redis操作被中断", e);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
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
     * Redis操作接口
     */
    @FunctionalInterface
    public interface RedisOperation<T> {
        T execute();
    }

    /**
     * 内部辅助方法：从 Redis 读取 Set 并反序列化
     */
    private <R> Set<R> getSetFromCache(String key, Class<R> type) {
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        // 使用 Optional 链式处理：判空 -> 转换 -> 返回
        return Optional.ofNullable(members)
                .filter(CollUtil::isNotEmpty)
                .map(set -> set.stream()
                        .map(m -> type == String.class ? type.cast(m) : JSONUtil.toBean(m, type))
                        .collect(Collectors.toSet()))
                .orElse(null); // 保持你原来的逻辑：查不到返回 null 以触发回填
    }
}