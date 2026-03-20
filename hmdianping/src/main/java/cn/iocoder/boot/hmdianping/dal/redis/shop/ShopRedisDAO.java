package cn.iocoder.boot.hmdianping.dal.redis.shop;

import cn.iocoder.boot.framework.common.util.JsonUtils;
import cn.iocoder.boot.hmdianping.dal.dataobject.shop.ShopDO;
import cn.iocoder.boot.hmdianping.enums.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ShopRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public ShopDO get(Long id) {
        String redisKey = formatKey(id);
        String value = stringRedisTemplate.opsForValue().get(redisKey);
        return JsonUtils.parseObject(value, ShopDO.class);
    }

    public void set(ShopDO shopDO) {
        String redisKey = formatKey(shopDO.getId());
        stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(shopDO));
    }

    public Boolean remove(Long id) {
        String redisKey = formatKey(id);
        return stringRedisTemplate.delete(redisKey);
    }

    private String formatKey(Long id) {
        return String.format(RedisKeyConstants.SHOP_ID, id);
    }
}
