package cn.iocoder.boot.framework.redis.core;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class RedisKeyBuild {
    /**
     * 实际使用的key
     * */
    private final String relKey;

    private RedisKeyBuild(String relKey) {
        this.relKey = relKey;
    }

    /**
     * 构建真实的key
     * @param redisKeyManage key的枚举
     * @param args 占位符的值
     * */
    public static String createRedisKey(RedisKeyManage redisKeyManage, Object... args){
        return String.format(redisKeyManage.getKey(),args);
    }

    public static String getRedisKey(RedisKeyManage redisKeyManage) {
        return redisKeyManage.getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedisKeyBuild that = (RedisKeyBuild) o;
        return relKey.equals(that.relKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relKey);
    }
}
