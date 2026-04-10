package cn.iocoder.boot.framework.redis.core.ratelimit;

import cn.iocoder.boot.framework.redis.core.RedisKeyBuild;
import cn.iocoder.boot.framework.redis.core.RedisKeyManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThresholdPenaltyPolicy implements RateLimitPenaltyPolicy {

    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillRateLimitConfigProperties props;

    public ThresholdPenaltyPolicy(StringRedisTemplate stringRedisTemplate, SeckillRateLimitConfigProperties props) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.props = props;
    }

    @Override
    public void apply(RateLimitContext context, Long result) {
        try {
            if (result == 10007L) {
                applyForIp(context);
            } else if (result == 10008L) {
                applyForUser(context);
            }
        } catch (Exception e) {
            log.info("Penalty policy apply failed: {}", e.getMessage());
        }
    }

    private void applyForIp(RateLimitContext ctx) {
        Long voucherId = ctx.getVoucherId();
        String clientIp = ctx.getClientIp();
        if (Objects.isNull(voucherId) || Objects.isNull(clientIp)) {
            return;
        }
        String violationKey = RedisKeyBuild.createRedisKey(
                RedisKeyManage.SECKILL_VIOLATION_IP_TAG_KEY, voucherId, clientIp);
        Long count = stringRedisTemplate.opsForValue().increment(violationKey, 1L);
        if (count == 1L) {
            stringRedisTemplate.expire(violationKey, props.getViolationWindowSeconds(), TimeUnit.SECONDS);
        }
        if (count >= props.getIpBlockThreshold()) {
            String blockKey = RedisKeyBuild.createRedisKey(
                    RedisKeyManage.SECKILL_BLOCK_IP_TAG_KEY, voucherId, clientIp);
            stringRedisTemplate.opsForValue().set(blockKey, "1", props.getIpBlockTtlSeconds(), TimeUnit.SECONDS);
            log.warn("Temporary banned IP: voucherId={}, ip={}, ttlSeconds={}, violationCount={}",
                    voucherId, clientIp, props.getIpBlockTtlSeconds(), count);
        }
    }

    private void applyForUser(RateLimitContext ctx) {
        Long voucherId = ctx.getVoucherId();
        Long userId = ctx.getUserId();
        if (Objects.isNull(voucherId) || Objects.isNull(userId)) {
            return;
        }
        String violationKey = RedisKeyBuild.createRedisKey(
                RedisKeyManage.SECKILL_VIOLATION_USER_TAG_KEY, voucherId, userId);
        Long count = stringRedisTemplate.opsForValue().increment(violationKey, 1L);
        if (count == 1L) {
            stringRedisTemplate.expire(violationKey, props.getViolationWindowSeconds(), TimeUnit.SECONDS);
        }
        if (count >= props.getUserBlockThreshold()) {
            String blockKey = RedisKeyBuild.createRedisKey(
                    RedisKeyManage.SECKILL_BLOCK_USER_TAG_KEY, voucherId, userId);
            stringRedisTemplate.opsForValue().set(blockKey, "1", props.getUserBlockTtlSeconds(), TimeUnit.SECONDS);
            log.warn("Temporary banned user: voucherId={}, userId={}, ttlSeconds={}, violationCount={}",
                    voucherId, userId, props.getUserBlockTtlSeconds(), count);
        }
    }
}