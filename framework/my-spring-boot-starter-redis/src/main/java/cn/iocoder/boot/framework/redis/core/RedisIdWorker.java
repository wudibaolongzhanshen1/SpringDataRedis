package cn.iocoder.boot.framework.redis.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP = 1640995200L;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final int COUNT_BITS = 32;

    public long nextId(String keyPrefix) {
        long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "icr:" + keyPrefix + ":" + date;
        long count = stringRedisTemplate.opsForValue().increment(key);
        return timestamp << COUNT_BITS | count;
    }
}
