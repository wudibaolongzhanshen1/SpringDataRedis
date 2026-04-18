package cn.iocoder.boot.framework.redis.monitor.aspect;

import cn.iocoder.boot.framework.redis.monitor.RedisMonitor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Redis监控切面
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "spring.redis.monitor.enabled", havingValue = "true", matchIfMissing = false)
public class RedisMonitorAspect {

    @Autowired
    @Lazy
    private RedisMonitor redisMonitor;

    /**
     * 切入点：所有RedisTemplate操作
     */
    @Pointcut("execution(* org.springframework.data.redis.core.RedisOperations.*(..))")
    public void redisOperations() {}

    /**
     * 切入点：所有StringRedisTemplate操作
     */
    @Pointcut("execution(* org.springframework.data.redis.core.StringRedisTemplate.*(..))")
    public void stringRedisOperations() {}

    /**
     * 切入点：自定义的Redis工具类（排除限流相关类以避免循环依赖）
     */
    @Pointcut("execution(* cn.iocoder.boot.framework.redis..*.*(..)) && " +
              "!execution(* cn.iocoder.boot.framework.redis.core.lua.TokenBucketRateLimitOperate.*(..)) && " +
              "!execution(* cn.iocoder.boot.framework.redis.core.ratelimit.RedisRateLimitHandler.*(..))")
    public void customRedisOperations() {}

    /**
     * 环绕通知：监控Redis操作（排除Redisson相关操作）
     */
    @Around("redisOperations() || stringRedisOperations() || customRedisOperations()")
    public Object monitorRedisOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String operation = className + "." + methodName;

        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Throwable throwable) {
            log.error("Redis操作失败: {} - {}", operation, throwable.getMessage());
            throw throwable;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            redisMonitor.recordCommand(operation, duration, success);

            // 记录详细日志（仅对慢查询或失败操作）
            if (!success || duration > 100) { // 100ms以上认为是慢查询
                log.debug("Redis操作: {}, 耗时: {}ms, 成功: {}",
                        operation, duration, success);
            }
        }
    }

    /**
     * 监控缓存命中率
     */
    @Around("@annotation(cn.iocoder.boot.framework.redis.monitor.annotation.CacheMonitor)")
    public Object monitorCache(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;
        Object result = null;

        try {
            result = joinPoint.proceed();
            cacheHit = (result != null);
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 这里可以记录缓存命中率统计
            // 实际实现中可能需要更复杂的逻辑来判断是否是缓存命中
            log.debug("缓存操作: {}, 耗时: {}ms, 命中: {}",
                    methodName, duration, cacheHit);
        }
    }

    /**
     * 监控分布式锁
     */
    @Around("@annotation(cn.iocoder.boot.framework.redis.monitor.annotation.LockMonitor)")
    public Object monitorLock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        long startTime = System.currentTimeMillis();
        boolean lockAcquired = false;
        long waitTime = 0;

        try {
            // 尝试获取锁
            Object[] args = joinPoint.getArgs();
            // 这里假设第一个参数是锁的key
            if (args.length > 0 && args[0] instanceof String) {
                String lockKey = (String) args[0];
                log.debug("尝试获取分布式锁: {}", lockKey);
            }

            Object result = joinPoint.proceed();
            lockAcquired = true;
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            waitTime = lockAcquired ? duration : -1;

            log.debug("分布式锁操作: {}, 耗时: {}ms, 获取成功: {}",
                    methodName, duration, lockAcquired);
        }
    }
}