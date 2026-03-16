package cn.iocoder.boot.hmdianping.dal.redis.token;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.boot.hmdianping.dal.dataobject.token.AccessTokenDO;
import cn.iocoder.boot.hmdianping.enums.RedisKeyConstants;
import cn.iocoder.boot.framework.common.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Repository
public class AccessTokenRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public AccessTokenDO get(String accessToken) {
        String redisKey = formatKey(accessToken);
        String value = stringRedisTemplate.opsForValue().get(redisKey);
        return JsonUtils.parseObject(value, AccessTokenDO.class);
    }

    public void set(AccessTokenDO accessTokenDO) {
        String redisKey = formatKey(accessTokenDO.getAccessToken());
        // 清理多余字段，避免缓存
        accessTokenDO.setUpdateTime(null).setCreateTime(null);
        long time = LocalDateTimeUtil.between(LocalDateTime.now(),accessTokenDO.getExpiresTime(), ChronoUnit.SECONDS);
        if (time > 0) {
            stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(accessTokenDO), time, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    private String formatKey(String accessToken) {
        return String.format(RedisKeyConstants.ACCESS_TOKEN, accessToken);
    }
}
