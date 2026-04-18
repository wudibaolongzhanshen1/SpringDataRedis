package cn.iocoder.boot.framework.common.exception.enums;

import cn.iocoder.boot.framework.common.exception.ErrorCode;

/**
 * Redis错误码常量
 * 错误码范围：[1000, 1999]
 */
public interface RedisErrorCodeConstants {

    // ========== Redis连接异常 ==========
    ErrorCode REDIS_CONNECTION_FAILED = new ErrorCode(1000, "Redis连接失败");
    ErrorCode REDIS_TIMEOUT = new ErrorCode(1001, "Redis操作超时");
    ErrorCode REDIS_COMMAND_FAILED = new ErrorCode(1002, "Redis命令执行失败");

    // ========== 分布式锁异常 ==========
    ErrorCode REDIS_LOCK_ACQUIRE_FAILED = new ErrorCode(1100, "获取分布式锁失败");
    ErrorCode REDIS_LOCK_TIMEOUT = new ErrorCode(1101, "获取分布式锁超时");
    ErrorCode REDIS_LOCK_NOT_HELD = new ErrorCode(1102, "当前线程未持有锁");

    // ========== 缓存异常 ==========
    ErrorCode CACHE_PENETRATION = new ErrorCode(1200, "缓存穿透");
    ErrorCode CACHE_BREAKDOWN = new ErrorCode(1201, "缓存击穿");
    ErrorCode CACHE_AVALANCHE = new ErrorCode(1202, "缓存雪崩");
    ErrorCode CACHE_WRITE_FAILED = new ErrorCode(1203, "缓存写入失败");
    ErrorCode CACHE_READ_FAILED = new ErrorCode(1204, "缓存读取失败");

    // ========== Lua脚本异常 ==========
    ErrorCode LUA_SCRIPT_EXECUTION_FAILED = new ErrorCode(1300, "Lua脚本执行失败");
    ErrorCode LUA_SCRIPT_COMPILE_FAILED = new ErrorCode(1301, "Lua脚本编译失败");

    // ========== 序列化异常 ==========
    ErrorCode REDIS_SERIALIZATION_FAILED = new ErrorCode(1400, "Redis序列化失败");
    ErrorCode REDIS_DESERIALIZATION_FAILED = new ErrorCode(1401, "Redis反序列化失败");

    // ========== 限流异常 ==========
    ErrorCode RATE_LIMIT_EXCEEDED = new ErrorCode(1500, "请求频率超限");
    ErrorCode RATE_LIMIT_CONFIG_ERROR = new ErrorCode(1501, "限流配置错误");

    // ========== 布隆过滤器异常 ==========
    ErrorCode BLOOM_FILTER_INIT_FAILED = new ErrorCode(1600, "布隆过滤器初始化失败");
    ErrorCode BLOOM_FILTER_OPERATION_FAILED = new ErrorCode(1601, "布隆过滤器操作失败");

}