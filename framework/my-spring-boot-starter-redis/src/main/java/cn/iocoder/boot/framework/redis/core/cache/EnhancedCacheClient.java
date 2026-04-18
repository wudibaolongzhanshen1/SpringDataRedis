package cn.iocoder.boot.framework.redis.core.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.boot.framework.common.exception.RedisException;
import cn.iocoder.boot.framework.common.exception.enums.RedisErrorCodeConstants;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 增强的缓存客户端，支持多级缓存和缓存雪崩防护
 */
@Slf4j
@Component
public class EnhancedCacheClient {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CacheConfig cacheConfig;

    // 本地缓存（Caffeine）
    private Cache<String, Object> localCache;

    // 缓存统计
    private final CacheStats cacheStats = new CacheStats();

    @PostConstruct
    public void init() {
        // 初始化本地缓存
        if (cacheConfig.isMultiLevelCacheEnabled()) {
            localCache = Caffeine.newBuilder()
                    .maximumSize(cacheConfig.getLocalCacheMaxSize())
                    .expireAfterWrite(cacheConfig.getLocalCacheTtl(), TimeUnit.SECONDS)
                    .recordStats()
                    .build();
            log.info("多级缓存已启用，本地缓存大小: {}, TTL: {}s",
                    cacheConfig.getLocalCacheMaxSize(), cacheConfig.getLocalCacheTtl());
        }
    }

    /**
     * 增强的缓存查询，支持多级缓存和雪崩防护
     */
    public <R, ID> R queryWithEnhancedCache(
            String keyPrefix, ID id, Class<R> returnType,
            Predicate<ID> bloomCheck, Function<ID, R> dbFallback,
            long ttl, TimeUnit unit) {

        // 缓存统计
        cacheStats.incrementTotalQueries();

        String key = keyPrefix + id;

        // 1. 检查本地缓存（如果启用）
        if (cacheConfig.isMultiLevelCacheEnabled()) {
            Object cached = localCache.getIfPresent(key);
            if (cached != null) {
                cacheStats.incrementLocalCacheHits();
                if (cached instanceof String && "".equals(cached)) {
                    return null; // 空值哨兵
                }
                return returnType.cast(cached);
            }
        }

        // 2. 【防穿透】执行传入的布隆检查逻辑
        if (!bloomCheck.test(id)) {
            log.info("布隆过滤器拦截：id {} 不存在", id);
            cacheStats.incrementBloomFilterBlocks();
            return null;
        }

        // 3. 检查Redis缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            cacheStats.incrementRedisCacheHits();
            R result = JSONUtil.toBean(json, returnType);
            // 写入本地缓存
            if (cacheConfig.isMultiLevelCacheEnabled() && result != null) {
                localCache.put(key, result);
            }
            return result;
        }

        // 处理防穿透的空值哨兵
        if ("".equals(json)) {
            // 写入本地缓存
            if (cacheConfig.isMultiLevelCacheEnabled()) {
                localCache.put(key, "");
            }
            return null;
        }

        // 4. 准备抢锁（防击穿）
        String lockKey = "lock:" + key;
        RLock rLock = redissonClient.getLock(lockKey);
        try {
            // 使用带随机范围的TTL防止雪崩
            long randomTtl = cacheConfig.getRandomTtl(unit.toSeconds(ttl));
            boolean getLock = rLock.tryLock(5, TimeUnit.SECONDS);
            if (!getLock) {
                log.warn("获取锁超时，放弃等待。lockKey={}", lockKey);
                cacheStats.incrementLockTimeouts();
                throw new RedisException(RedisErrorCodeConstants.REDIS_LOCK_TIMEOUT);
            }

            // 5. 双重检查缓存
            json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                R result = JSONUtil.toBean(json, returnType);
                if (cacheConfig.isMultiLevelCacheEnabled() && result != null) {
                    localCache.put(key, result);
                }
                return result;
            }
            if ("".equals(json)) {
                if (cacheConfig.isMultiLevelCacheEnabled()) {
                    localCache.put(key, "");
                }
                return null;
            }

            // 6. 查询数据库
            R dbResult = dbFallback.apply(id);
            cacheStats.incrementDbQueries();

            // 7. 回写缓存
            if (dbResult == null) {
                // 空值缓存，使用配置的TTL
                stringRedisTemplate.opsForValue().set(key, "",
                        cacheConfig.getNullValueTtl(), TimeUnit.SECONDS);
                if (cacheConfig.isMultiLevelCacheEnabled()) {
                    localCache.put(key, "");
                }
            } else {
                // 正常缓存，使用带随机范围的TTL
                stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(dbResult),
                        randomTtl, TimeUnit.SECONDS);
                if (cacheConfig.isMultiLevelCacheEnabled()) {
                    localCache.put(key, dbResult);
                }
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
     * 批量查询缓存
     */
    public <R, ID> Map<ID, R> batchQuery(
            String keyPrefix, List<ID> ids, Class<R> returnType,
            Function<List<ID>, Map<ID, R>> dbFallback,
            long ttl, TimeUnit unit) {

        Map<ID, R> result = new HashMap<>();
        List<ID> missingIds = new ArrayList<>();

        // 1. 批量从Redis查询
        List<String> keys = ids.stream()
                .map(id -> keyPrefix + id)
                .collect(Collectors.toList());

        List<String> cachedValues = stringRedisTemplate.opsForValue().multiGet(keys);
        if (cachedValues != null) {
            for (int i = 0; i < ids.size(); i++) {
                ID id = ids.get(i);
                String cachedValue = cachedValues.get(i);
                if (StrUtil.isNotBlank(cachedValue)) {
                    if ("".equals(cachedValue)) {
                        result.put(id, null); // 空值哨兵
                    } else {
                        result.put(id, JSONUtil.toBean(cachedValue, returnType));
                    }
                } else {
                    missingIds.add(id);
                }
            }
        } else {
            missingIds.addAll(ids);
        }

        // 2. 查询缺失的数据
        if (!missingIds.isEmpty()) {
            Map<ID, R> dbResults = dbFallback.apply(missingIds);
            result.putAll(dbResults);

            // 3. 批量回写缓存
            Map<String, String> cacheMap = new HashMap<>();
            for (ID id : missingIds) {
                String key = keyPrefix + id;
                R value = dbResults.get(id);
                if (value == null) {
                    cacheMap.put(key, ""); // 空值哨兵
                } else {
                    cacheMap.put(key, JSONUtil.toJsonStr(value));
                }
            }

            if (!cacheMap.isEmpty()) {
                stringRedisTemplate.opsForValue().multiSet(cacheMap);
                // 设置过期时间
                long randomTtl = cacheConfig.getRandomTtl(unit.toSeconds(ttl));
                cacheMap.keySet().forEach(key ->
                        stringRedisTemplate.expire(key, randomTtl, TimeUnit.SECONDS));
            }
        }

        return result;
    }

    /**
     * 删除缓存（同时删除本地缓存和Redis缓存）
     */
    public void deleteCache(String key) {
        // 删除本地缓存
        if (cacheConfig.isMultiLevelCacheEnabled()) {
            localCache.invalidate(key);
        }
        // 删除Redis缓存
        stringRedisTemplate.delete(key);
        cacheStats.incrementCacheDeletes();
    }

    /**
     * 批量删除缓存
     */
    public void batchDeleteCache(Collection<String> keys) {
        // 删除本地缓存
        if (cacheConfig.isMultiLevelCacheEnabled()) {
            localCache.invalidateAll(keys);
        }
        // 删除Redis缓存
        stringRedisTemplate.delete(keys);
        cacheStats.incrementCacheDeletes(keys.size());
    }

    /**
     * 清除所有本地缓存
     */
    public void clearLocalCache() {
        if (cacheConfig.isMultiLevelCacheEnabled()) {
            localCache.invalidateAll();
            log.info("本地缓存已清除");
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        return cacheStats;
    }

    /**
     * 缓存统计类
     */
    @Data
    public static class CacheStats {
        private long totalQueries = 0;
        private long localCacheHits = 0;
        private long redisCacheHits = 0;
        private long dbQueries = 0;
        private long bloomFilterBlocks = 0;
        private long lockTimeouts = 0;
        private long cacheDeletes = 0;

        public void incrementTotalQueries() {
            totalQueries++;
        }

        public void incrementLocalCacheHits() {
            localCacheHits++;
        }

        public void incrementRedisCacheHits() {
            redisCacheHits++;
        }

        public void incrementDbQueries() {
            dbQueries++;
        }

        public void incrementBloomFilterBlocks() {
            bloomFilterBlocks++;
        }

        public void incrementLockTimeouts() {
            lockTimeouts++;
        }

        public void incrementCacheDeletes() {
            cacheDeletes++;
        }

        public void incrementCacheDeletes(long count) {
            cacheDeletes += count;
        }

        /**
         * 计算总命中率
         */
        public double getTotalHitRate() {
            if (totalQueries == 0) return 0.0;
            return (double) (localCacheHits + redisCacheHits) / totalQueries;
        }

        /**
         * 计算本地缓存命中率
         */
        public double getLocalCacheHitRate() {
            if (totalQueries == 0) return 0.0;
            return (double) localCacheHits / totalQueries;
        }

        /**
         * 计算Redis缓存命中率
         */
        public double getRedisCacheHitRate() {
            if (totalQueries == 0) return 0.0;
            return (double) redisCacheHits / totalQueries;
        }
    }
}