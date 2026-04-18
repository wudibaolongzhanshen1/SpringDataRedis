package cn.iocoder.boot.framework.redis.core.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 */
@Data
@ConfigurationProperties(prefix = "spring.cache")
public class CacheConfig {

    /**
     * 默认缓存过期时间（秒）
     */
    private long defaultTtl = 300;

    /**
     * 默认时间单位
     */
    private TimeUnit defaultTimeUnit = TimeUnit.SECONDS;

    /**
     * 空值缓存过期时间（秒）
     */
    private long nullValueTtl = 120;

    /**
     * 缓存雪崩防护：过期时间随机范围（秒）
     */
    private int ttlRandomRange = 60;

    /**
     * 是否启用多级缓存
     */
    private boolean multiLevelCacheEnabled = false;

    /**
     * 本地缓存最大大小
     */
    private int localCacheMaxSize = 1000;

    /**
     * 本地缓存过期时间（秒）
     */
    private long localCacheTtl = 60;

    /**
     * 是否启用缓存预热
     */
    private boolean cacheWarmUpEnabled = false;

    /**
     * 缓存预热线程数
     */
    private int warmUpThreads = 4;

    /**
     * 缓存统计是否启用
     */
    private boolean statsEnabled = true;

    /**
     * 获取带随机范围的过期时间（防止缓存雪崩）
     */
    public long getRandomTtl(long baseTtl) {
        if (ttlRandomRange <= 0) {
            return baseTtl;
        }
        long randomOffset = (long) (Math.random() * ttlRandomRange);
        return baseTtl + randomOffset;
    }

    /**
     * 获取带随机范围的过期时间（使用默认时间单位）
     */
    public long getRandomTtl() {
        return getRandomTtl(defaultTtl);
    }

}