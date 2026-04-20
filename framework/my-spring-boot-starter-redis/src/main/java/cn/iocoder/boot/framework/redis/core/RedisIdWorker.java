package cn.iocoder.boot.framework.redis.core;


import cn.iocoder.boot.framework.common.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * ID生成器
 * 支持两种模式：
 * 1. 雪花算法（默认）：本地生成，无Redis依赖，高性能
 * 2. Redis生成：基于日期和自增，适合分布式环境但性能较低
 */
@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP = 1640995200L;
    private static final int COUNT_BITS = 32;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Value("${id.generator.type:snowflake}")
    private String idGeneratorType;

    /**
     * 生成下一个ID
     * @param keyPrefix 业务前缀（仅Redis模式使用）
     * @return 生成的ID
     */
    public long nextId(String keyPrefix) {
        if ("redis".equalsIgnoreCase(idGeneratorType)) {
            return nextIdByRedis(keyPrefix);
        } else {
            // 默认使用雪花算法
            return nextIdBySnowflake();
        }
    }

    /**
     * 使用Redis生成ID（基于日期和自增）
     */
    private long nextIdByRedis(String keyPrefix) {
        long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "icr:" + keyPrefix + ":" + date;
        long count = stringRedisTemplate.opsForValue().increment(key);
        return timestamp << COUNT_BITS | count;
    }

    /**
     * 使用雪花算法生成ID
     */
    private long nextIdBySnowflake() {
        if (snowflakeIdGenerator == null) {
            throw new IllegalStateException("SnowflakeIdGenerator is not available. Please check if it's properly configured.");
        }
        return snowflakeIdGenerator.nextId();
    }

    /**
     * 获取当前使用的ID生成器类型
     */
    public String getIdGeneratorType() {
        return idGeneratorType;
    }
}
