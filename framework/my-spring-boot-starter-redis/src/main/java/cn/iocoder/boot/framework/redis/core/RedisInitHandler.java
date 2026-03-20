package cn.iocoder.boot.framework.redis.core;

import cn.iocoder.boot.framework.common.api.user.UserApi;
import cn.iocoder.boot.framework.common.dto.user.UserDTO;
import cn.iocoder.boot.framework.common.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class RedisInitHandler implements InitializingBean {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserApi userApi;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        RLock lock = redissonClient.getLock("redis_init_lock");
        boolean success = lock.tryLock(1, java.util.concurrent.TimeUnit.SECONDS);
        if (success) {
            try {
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey("init_finished_flag"))) {
                    log.warn("[afterPropertiesSet][Redis 数据已经被其他服务器初始化完成，跳过]");
                    return;
                }
                log.info("[afterPropertiesSet][开始初始化 Redis 数据]");
                List<UserDTO> userDTOS = userApi.selectAll();
                for (UserDTO userDTO : userDTOS) {
                    stringRedisTemplate.opsForValue().set("user:" + userDTO.getId(), JsonUtils.toJsonString(userDTO));
                }
                // 设置 24 小时过期
                stringRedisTemplate.opsForValue().set("init_finished_flag", "true", 24, TimeUnit.MINUTES);
            } finally {
                lock.unlock();
            }
        }else{
            log.warn("[afterPropertiesSet][没有获取到锁，跳过 Redis 数据初始化]");
        }
    }
}
