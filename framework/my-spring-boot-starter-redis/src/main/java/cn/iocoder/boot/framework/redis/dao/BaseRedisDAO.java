package cn.iocoder.boot.framework.redis.dao;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.boot.framework.common.exception.RedisException;
import cn.iocoder.boot.framework.common.exception.enums.RedisErrorCodeConstants;
import cn.iocoder.boot.framework.redis.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用的Redis DAO基类
 *
 * @param <T> 实体类型
 * @param <ID> ID类型
 */
@Slf4j
public abstract class BaseRedisDAO<T, ID> {

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected RedisUtils redisUtils;

    /**
     * 获取实体类类型
     */
    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public BaseRedisDAO() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            this.entityClass = (Class<T>) typeArguments[0];
        } else {
            throw new IllegalStateException("BaseRedisDAO must be parameterized with entity type");
        }
    }

    /**
     * 生成Redis键
     */
    protected abstract String buildKey(ID id);

    /**
     * 生成批量Redis键
     */
    protected List<String> buildKeys(Collection<ID> ids) {
        return ids.stream()
                .map(this::buildKey)
                .collect(Collectors.toList());
    }

    /**
     * 序列化实体
     */
    protected String serialize(T entity) {
        if (entity == null) {
            return "";
        }
        try {
            return JSONUtil.toJsonStr(entity);
        } catch (Exception e) {
            throw new RedisException(RedisErrorCodeConstants.REDIS_SERIALIZATION_FAILED);
        }
    }

    /**
     * 反序列化实体
     */
    protected T deserialize(String json) {
        if (StrUtil.isBlank(json) || "".equals(json)) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, entityClass);
        } catch (Exception e) {
            log.error("反序列化失败: {}", json, e);
            throw new RedisException(RedisErrorCodeConstants.REDIS_DESERIALIZATION_FAILED);
        }
    }

    /**
     * 获取单个实体
     */
    public T get(ID id) {
        String key = buildKey(id);
        String json = redisUtils.getWithRetry(key);
        return deserialize(json);
    }

    /**
     * 批量获取实体
     */
    public Map<ID, T> batchGet(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> keys = buildKeys(ids);
        List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);

        Map<ID, T> result = new HashMap<>();
        int index = 0;
        for (ID id : ids) {
            String value = values != null ? values.get(index) : null;
            result.put(id, deserialize(value));
            index++;
        }
        return result;
    }

    /**
     * 保存实体（无过期时间）
     */
    public void set(T entity, ID id) {
        if (entity == null) {
            return;
        }
        String key = buildKey(id);
        String value = serialize(entity);
        redisUtils.setWithRetry(key, value, 0, TimeUnit.SECONDS);
    }

    /**
     * 保存实体（带过期时间）
     */
    public void set(T entity, ID id, long timeout, TimeUnit unit) {
        if (entity == null) {
            return;
        }
        String key = buildKey(id);
        String value = serialize(entity);
        redisUtils.setWithRetry(key, value, timeout, unit);
    }

    /**
     * 批量保存实体
     */
    public void batchSet(Map<ID, T> entities, long timeout, TimeUnit unit) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        Map<String, String> keyValueMap = new HashMap<>();
        for (Map.Entry<ID, T> entry : entities.entrySet()) {
            String key = buildKey(entry.getKey());
            String value = serialize(entry.getValue());
            keyValueMap.put(key, value);
        }

        stringRedisTemplate.opsForValue().multiSet(keyValueMap);
        // 设置过期时间
        for (String key : keyValueMap.keySet()) {
            stringRedisTemplate.expire(key, timeout, unit);
        }
    }

    /**
     * 删除实体
     */
    public boolean delete(ID id) {
        String key = buildKey(id);
        Boolean result = redisUtils.deleteWithRetry(key);
        return result != null && result;
    }

    /**
     * 批量删除实体
     */
    public long batchDelete(Collection<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        List<String> keys = buildKeys(ids);
        Long count = stringRedisTemplate.delete(keys);
        return count != null ? count : 0;
    }

    /**
     * 设置过期时间
     */
    public boolean expire(ID id, long timeout, TimeUnit unit) {
        String key = buildKey(id);
        Boolean result = redisUtils.expireWithRetry(key, timeout, unit);
        return result != null && result;
    }

    /**
     * 检查实体是否存在
     */
    public boolean exists(ID id) {
        String key = buildKey(id);
        Boolean result = redisUtils.hasKeyWithRetry(key);
        return result != null && result;
    }

    /**
     * 获取并删除实体
     */
    public T getAndDelete(ID id) {
        String key = buildKey(id);
        String json = redisUtils.getWithRetry(key);
        T entity = deserialize(json);
        if (entity != null) {
            redisUtils.deleteWithRetry(key);
        }
        return entity;
    }

    /**
     * 缓存查询模板
     */
    public T cacheQuery(ID id, Function<ID, T> dbLoader, long timeout, TimeUnit unit) {
        // 先查缓存
        T cached = get(id);
        if (cached != null) {
            return cached;
        }

        // 查数据库
        T dbResult = dbLoader.apply(id);
        if (dbResult != null) {
            // 写入缓存
            set(dbResult, id, timeout, unit);
        } else {
            // 空值缓存，防止缓存穿透
            set(null, id, Math.min(timeout, 120), unit); // 空值缓存最多2分钟
        }

        return dbResult;
    }

    /**
     * 批量缓存查询模板
     */
    public Map<ID, T> batchCacheQuery(Collection<ID> ids, Function<Collection<ID>, Map<ID, T>> dbLoader,
                                     long timeout, TimeUnit unit) {
        // 先批量查缓存
        Map<ID, T> cachedResults = batchGet(ids);

        // 找出未命中的ID
        List<ID> missingIds = ids.stream()
                .filter(id -> !cachedResults.containsKey(id) || cachedResults.get(id) == null)
                .collect(Collectors.toList());

        if (!missingIds.isEmpty()) {
            // 批量查数据库
            Map<ID, T> dbResults = dbLoader.apply(missingIds);

            // 批量写入缓存
            batchSet(dbResults, timeout, unit);

            // 合并结果
            cachedResults.putAll(dbResults);
        }

        return cachedResults;
    }
}