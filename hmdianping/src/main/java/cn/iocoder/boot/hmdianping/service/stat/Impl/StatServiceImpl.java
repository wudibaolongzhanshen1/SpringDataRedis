package cn.iocoder.boot.hmdianping.service.stat.Impl;

import cn.iocoder.boot.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.boot.hmdianping.enums.RedisKeyConstants;
import cn.iocoder.boot.hmdianping.service.stat.StatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class StatServiceImpl implements StatService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void recordUV(String bizName, String targetId) {
        // 1. 获取当前用户（从拦截器/ThreadLocal获取）
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        // 2. 构造 Key (按天统计)
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = String.format(RedisKeyConstants.UV_STAT_KEY, bizName, targetId, today);
        // 3. 写入 Redis
        // PFADD 命令：如果 userId 已存在，则不占空间，不增加计数
        redisTemplate.opsForHyperLogLog().add(key, userId.toString());
        log.info("用户 {} 访问了 {}-{}, 已计入 UV", userId, bizName, targetId);
    }

    @Override
    public Long getUVCount(String bizName, String targetId, String date) {
        String key = String.format(RedisKeyConstants.UV_STAT_KEY, bizName, targetId, date);
        // PFCOUNT 命令：获取估算的去重总数
        return redisTemplate.opsForHyperLogLog().size(key);
    }
}
