package cn.iocoder.boot.framework.redis.core.util;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;

@Component
public class RedisBatchUtils {

    /**
     * 通用 Pipeline 批量执行工具
     *
     * @param redisTemplate Redis 模版
     * @param dataList      需要处理的数据集合
     * @param action        具体的 Redis 动作逻辑 (connection, data) -> { ... }
     * @param <T>           数据类型
     */
    public static <T> void executeBatch(StringRedisTemplate redisTemplate, List<T> dataList, BiConsumer<RedisConnection, T> action) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (T data : dataList) {
                // 具体的逻辑由调用者实现
                action.accept(connection, data);
            }
            return null; // Pipeline 模式下必须返回 null
        });
    }
}