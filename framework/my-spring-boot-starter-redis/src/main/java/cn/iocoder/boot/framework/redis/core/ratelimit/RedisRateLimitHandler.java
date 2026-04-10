package cn.iocoder.boot.framework.redis.core.ratelimit;

import cn.iocoder.boot.framework.common.exception.ServiceException;
import cn.iocoder.boot.framework.redis.core.RedisKeyBuild;
import cn.iocoder.boot.framework.redis.core.RedisKeyManage;
import cn.iocoder.boot.framework.redis.core.listener.RateLimitEventListener;
import cn.iocoder.boot.framework.redis.core.lua.TokenBucketRateLimitOperate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @program: 黑马点评-plus升级版实战项目。添加 阿星不是程序员 微信，添加时备注 点评 来获取项目的完整资料
 * @description: 限流执行 接口实现
 * @author: 阿星不是程序员
 **/
public class RedisRateLimitHandler implements RateLimitHandler {

    private final SeckillRateLimitConfigProperties seckillRateLimitConfigProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final TokenBucketRateLimitOperate tokenBucketRateLimitOperate;
    private final RateLimitEventListener rateLimitEventListener;
    private final RateLimitPenaltyPolicy rateLimitPenaltyPolicy;

    public RedisRateLimitHandler(StringRedisTemplate stringRedisTemplate,
                                 SeckillRateLimitConfigProperties seckillRateLimitConfigProperties,
                                 TokenBucketRateLimitOperate tokenBucketRateLimitOperate,
                                 RateLimitEventListener rateLimitEventListener, RateLimitPenaltyPolicy rateLimitPenaltyPolicy) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.seckillRateLimitConfigProperties = seckillRateLimitConfigProperties;
        this.tokenBucketRateLimitOperate = tokenBucketRateLimitOperate;
        this.rateLimitEventListener = rateLimitEventListener;
        this.rateLimitPenaltyPolicy = rateLimitPenaltyPolicy;
    }

    @Override
    public void execute(Long voucherId,
                        Long userId,
                        RateLimitScene scene) {
        String clientIp = resolveClientIp();

        if (isWhitelisted(userId, clientIp)) {
            return;
        }
        checkBans(voucherId, userId, clientIp);
        int ipLimitWindowMillis = resolveIpWindow(scene);
        int ipLimitMaxAttempts = resolveIpMaxAttempts(scene);
        int userLimitWindowMillis = resolveUserWindow(scene);
        int userLimitMaxAttempts = resolveUserMaxAttempts(scene);
        boolean useSliding = resolveSliding();
        List<String> keys = buildRateLimitKeys(voucherId, userId, clientIp, useSliding);
        String[] args = buildArgs(ipLimitWindowMillis, ipLimitMaxAttempts, userLimitWindowMillis, userLimitMaxAttempts);
        RateLimitContext ctx = buildContext(voucherId, userId, clientIp, keys, useSliding,
                ipLimitWindowMillis, ipLimitMaxAttempts, userLimitWindowMillis, userLimitMaxAttempts);
        safeBeforeExecute(ctx);
        Long result = executeLua(keys, args);
        ctx.setResult(result);
        handleResult(ctx);
    }

    private int resolveIpWindow(RateLimitScene scene) {
        SeckillRateLimitConfigProperties.EndpointLimit ep =
                scene == RateLimitScene.ISSUE_TOKEN ?
                        seckillRateLimitConfigProperties.getIssue() :
                        seckillRateLimitConfigProperties.getSeckill();
        Integer v = ep != null ? ep.getIpWindowMillis() : null;
        return v != null ? v : seckillRateLimitConfigProperties.getIpWindowMillis();
    }

    private int resolveIpMaxAttempts(RateLimitScene scene) {
        SeckillRateLimitConfigProperties.EndpointLimit ep =
                scene == RateLimitScene.ISSUE_TOKEN ?
                        seckillRateLimitConfigProperties.getIssue() :
                        seckillRateLimitConfigProperties.getSeckill();
        Integer v = ep != null ? ep.getIpMaxAttempts() : null;
        return v != null ? v : seckillRateLimitConfigProperties.getIpMaxAttempts();
    }

    private int resolveUserWindow(RateLimitScene scene) {
        SeckillRateLimitConfigProperties.EndpointLimit ep =
                scene == RateLimitScene.ISSUE_TOKEN ?
                        seckillRateLimitConfigProperties.getIssue() :
                        seckillRateLimitConfigProperties.getSeckill();
        Integer v = ep != null ? ep.getUserWindowMillis() : null;
        return v != null ? v : seckillRateLimitConfigProperties.getUserWindowMillis();
    }

    private int resolveUserMaxAttempts(RateLimitScene scene) {
        SeckillRateLimitConfigProperties.EndpointLimit ep =
                scene == RateLimitScene.ISSUE_TOKEN ?
                        seckillRateLimitConfigProperties.getIssue() :
                        seckillRateLimitConfigProperties.getSeckill();
        Integer v = ep != null ? ep.getUserMaxAttempts() : null;
        return v != null ? v : seckillRateLimitConfigProperties.getUserMaxAttempts();
    }

    private boolean resolveSliding() {
        return seckillRateLimitConfigProperties.getEnableSlidingWindow();
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isEmpty()) {
                String[] parts = xff.split(",");
                if (parts.length > 0) {
                    String ip = parts[0].trim();
                    if (!ip.isEmpty()) {
                        return ip;
                    }
                }
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isEmpty()) {
                return realIp;
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isWhitelisted(Long userId,
                                  String clientIp) {
        try {
            return (clientIp != null && seckillRateLimitConfigProperties.getIpWhitelist() != null
                    && seckillRateLimitConfigProperties.getIpWhitelist().contains(clientIp))
                    || (userId != null && seckillRateLimitConfigProperties.getUserWhitelist() != null
                    && seckillRateLimitConfigProperties.getUserWhitelist().contains(userId));
        } catch (Exception e) {
            return false;
        }
    }

    private void checkBans(Long voucherId,
                           Long userId,
                           String clientIp) {

        if (Objects.nonNull(clientIp)) {
            boolean ipBlocked = stringRedisTemplate.hasKey(
                    RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_BLOCK_IP_TAG_KEY, voucherId, clientIp));
            if (ipBlocked) {
                throw new ServiceException(500, "操作频繁，IP被阻塞，请稍后再试");
            }
        }
        boolean userBlocked = stringRedisTemplate.hasKey(
                RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_BLOCK_USER_TAG_KEY, voucherId, userId));
        if (userBlocked) {
            throw new ServiceException(500, "操作频繁，该用户被阻塞，请稍后再试");
        }
    }

    private List<String> buildRateLimitKeys(Long voucherId,
                                            Long userId,
                                            String clientIp,
                                            boolean useSliding) {
        List<String> keys = new ArrayList<>(2);
        if (Objects.nonNull(clientIp)) {
            String ipKey = useSliding
                    ? RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_LIMIT_IP_SW_TAG_KEY, voucherId, clientIp)
                    : RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_LIMIT_IP_TB_TAG_KEY, voucherId, clientIp);
            keys.add(ipKey);
        }
        String userKey = useSliding
                ? RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_LIMIT_USER_SW_TAG_KEY, voucherId, userId)
                : RedisKeyBuild.createRedisKey(RedisKeyManage.SECKILL_LIMIT_USER_TB_TAG_KEY, voucherId, userId);
        keys.add(userKey);
        return keys;
    }

    private String[] buildArgs(int ipWindowMillis,
                               int ipMaxAttempts,
                               int userWindowMillis,
                               int userMaxAttempts) {
        String[] args = new String[4];
        args[0] = String.valueOf(ipWindowMillis);
        args[1] = String.valueOf(ipMaxAttempts);
        args[2] = String.valueOf(userWindowMillis);
        args[3] = String.valueOf(userMaxAttempts);
        return args;
    }

    private RateLimitContext buildContext(Long voucherId,
                                          Long userId,
                                          String clientIp,
                                          List<String> keys,
                                          boolean useSliding,
                                          int ipWindowMillis, int ipMaxAttempts,
                                          int userWindowMillis, int userMaxAttempts) {
        return new RateLimitContext(
                voucherId,
                userId,
                clientIp,
                keys,
                useSliding,
                ipWindowMillis,
                ipMaxAttempts,
                userWindowMillis,
                userMaxAttempts
        );
    }

    private void safeBeforeExecute(RateLimitContext ctx) {
        rateLimitEventListener.onBeforeExecute(ctx);
    }

    private Long executeLua(List<String> keys, String[] args) {
        return tokenBucketRateLimitOperate.execute(keys, args);
    }

    private void handleResult(RateLimitContext ctx) {
        Long result = ctx.getResult();
        if (result==0L) {
            rateLimitEventListener.onAllowed(ctx);
            return;
        }
        if (result==10007L) {
            rateLimitEventListener.onBlocked(ctx, "请求IP过于频繁，稍后再试");
            rateLimitPenaltyPolicy.apply(ctx, 10007L);
            throw new ServiceException(500,"请求IP过于频繁，稍后再试");
        }
        if (result==10008L) {
            rateLimitEventListener.onBlocked(ctx, "用户操作过于频繁，稍后再试");
            rateLimitPenaltyPolicy.apply(ctx, 10008L);
            throw new ServiceException(500,"用户操作过于频繁，稍后再试");
        }
        throw new ServiceException(500, "未知的限流结果: " + result);
    }
}
