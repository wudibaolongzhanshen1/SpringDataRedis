package cn.iocoder.boot.framework.redis.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis监控配置
 */
@Data
@ConfigurationProperties(prefix = "spring.redis.monitor")
public class RedisMonitorConfig {

    /**
     * 是否启用监控
     */
    private boolean enabled = true;

    /**
     * 监控指标前缀
     */
    private String metricPrefix = "redis";

    /**
     * 连接池监控间隔（秒）
     */
    private int poolStatsInterval = 30;

    /**
     * 内存监控间隔（秒）
     */
    private int memoryStatsInterval = 60;

    /**
     * 命令统计监控间隔（秒）
     */
    private int commandStatsInterval = 30;

    /**
     * 慢查询监控阈值（毫秒）
     */
    private long slowQueryThreshold = 100;

    /**
     * 是否启用健康检查
     */
    private boolean healthCheckEnabled = true;

    /**
     * 健康检查间隔（秒）
     */
    private int healthCheckInterval = 30;

    /**
     * 告警配置
     */
    private AlertConfig alert = new AlertConfig();

    @Data
    public static class AlertConfig {
        /**
         * 内存使用率告警阈值（百分比）
         */
        private double memoryUsageThreshold = 80.0;

        /**
         * 连接数使用率告警阈值（百分比）
         */
        private double connectionUsageThreshold = 90.0;

        /**
         * 慢查询数量告警阈值（每分钟）
         */
        private int slowQueryThreshold = 10;

        /**
         * 错误率告警阈值（百分比）
         */
        private double errorRateThreshold = 5.0;
    }
}