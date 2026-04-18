package cn.iocoder.boot.framework.redis.monitor;

import io.micrometer.core.instrument.*;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis监控器
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.redis.monitor.enabled", havingValue = "true", matchIfMissing = false)
public class RedisMonitor {

    @Autowired
    @Lazy
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    @Lazy
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private RedisMonitorConfig monitorConfig;

    @Autowired
    private MeterRegistry meterRegistry;

    // 监控指标
    private Counter totalCommands;
    private Counter failedCommands;
    private Timer commandTimer;
    private Gauge memoryUsage;
    private Gauge connectionCount;
    private Counter slowQueries;

    // 统计信息
    private final AtomicLong totalCommandCount = new AtomicLong(0);
    private final AtomicLong failedCommandCount = new AtomicLong(0);
    private final AtomicLong slowQueryCount = new AtomicLong(0);
    private final Map<String, AtomicLong> commandStats = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (!monitorConfig.isEnabled()) {
            log.info("Redis监控未启用");
            return;
        }

        // 初始化监控指标
        String prefix = monitorConfig.getMetricPrefix();

        totalCommands = Counter.builder(prefix + ".commands.total")
                .description("Redis命令执行总数")
                .register(meterRegistry);

        failedCommands = Counter.builder(prefix + ".commands.failed")
                .description("Redis命令执行失败数")
                .register(meterRegistry);

        commandTimer = Timer.builder(prefix + ".commands.duration")
                .description("Redis命令执行时间")
                .publishPercentiles(0.5, 0.95, 0.99) // 50%, 95%, 99%分位
                .register(meterRegistry);

        slowQueries = Counter.builder(prefix + ".queries.slow")
                .description("Redis慢查询数量")
                .register(meterRegistry);

        // 动态指标
        memoryUsage = Gauge.builder(prefix + ".memory.usage", this::getMemoryUsage)
                .description("Redis内存使用率")
                .register(meterRegistry);

        connectionCount = Gauge.builder(prefix + ".connections.active", this::getActiveConnections)
                .description("Redis活跃连接数")
                .register(meterRegistry);

        log.info("Redis监控已初始化");
    }

    /**
     * 记录命令执行
     */
    public void recordCommand(String command, long duration, boolean success) {
        if (!monitorConfig.isEnabled()) {
            return;
        }

        totalCommands.increment();
        totalCommandCount.incrementAndGet();

        if (!success) {
            failedCommands.increment();
            failedCommandCount.incrementAndGet();
        }

        commandTimer.record(duration, TimeUnit.MILLISECONDS);

        // 记录慢查询
        if (duration > monitorConfig.getSlowQueryThreshold()) {
            slowQueries.increment();
            slowQueryCount.incrementAndGet();
            log.warn("Redis慢查询: command={}, duration={}ms", command, duration);
        }

        // 统计命令类型
        commandStats.computeIfAbsent(command, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * 获取内存使用率
     */
    private double getMemoryUsage() {
        try {
            Properties info = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("memory");
            String usedMemory = info.getProperty("used_memory");
            String maxMemory = info.getProperty("maxmemory");

            if (usedMemory != null && maxMemory != null && !"0".equals(maxMemory)) {
                double used = Double.parseDouble(usedMemory);
                double max = Double.parseDouble(maxMemory);
                return (used / max) * 100;
            }
        } catch (Exception e) {
            log.error("获取Redis内存使用率失败", e);
        }
        return 0.0;
    }

    /**
     * 获取活跃连接数
     */
    private int getActiveConnections() {
        try {
            Properties info = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("clients");
            String connectedClients = info.getProperty("connected_clients");
            return connectedClients != null ? Integer.parseInt(connectedClients) : 0;
        } catch (Exception e) {
            log.error("获取Redis连接数失败", e);
        }
        return 0;
    }

    /**
     * 定期收集统计信息
     */
    @Scheduled(fixedDelayString = "${spring.redis.monitor.poolStatsInterval:30000}")
    public void collectStats() {
        if (!monitorConfig.isEnabled()) {
            return;
        }

        try {
            // 收集连接池统计
            collectConnectionPoolStats();

            // 收集内存统计
            collectMemoryStats();

            // 收集命令统计
            collectCommandStats();

            // 检查告警
            checkAlerts();

        } catch (Exception e) {
            log.error("收集Redis监控数据失败", e);
        }
    }

    /**
     * 收集连接池统计
     */
    private void collectConnectionPoolStats() {
        try {
            Properties info = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("stats");

            // 记录连接池指标
            Tags tags = Tags.of("type", "lettuce");
            meterRegistry.gauge(monitorConfig.getMetricPrefix() + ".pool.active",
                    tags, this, RedisMonitor::getPoolActiveCount);
            meterRegistry.gauge(monitorConfig.getMetricPrefix() + ".pool.idle",
                    tags, this, RedisMonitor::getPoolIdleCount);

        } catch (Exception e) {
            log.error("收集连接池统计失败", e);
        }
    }

    /**
     * 获取连接池活跃数（模拟）
     */
    private int getPoolActiveCount() {
        // 这里需要根据实际的连接池实现获取
        // 对于Lettuce，可以通过RedisConnectionFactory获取
        return 0;
    }

    /**
     * 获取连接池空闲数（模拟）
     */
    private int getPoolIdleCount() {
        // 这里需要根据实际的连接池实现获取
        return 0;
    }

    /**
     * 收集内存统计
     */
    private void collectMemoryStats() {
        try {
            Properties info = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("memory");

            // 记录内存指标
            String usedMemory = info.getProperty("used_memory");
            String usedMemoryRss = info.getProperty("used_memory_rss");
            String usedMemoryPeak = info.getProperty("used_memory_peak");

            if (usedMemory != null) {
                meterRegistry.gauge(monitorConfig.getMetricPrefix() + ".memory.used",
                        Tags.empty(), Double.parseDouble(usedMemory));
            }
            if (usedMemoryRss != null) {
                meterRegistry.gauge(monitorConfig.getMetricPrefix() + ".memory.rss",
                        Tags.empty(), Double.parseDouble(usedMemoryRss));
            }
            if (usedMemoryPeak != null) {
                meterRegistry.gauge(monitorConfig.getMetricPrefix() + ".memory.peak",
                        Tags.empty(), Double.parseDouble(usedMemoryPeak));
            }

        } catch (Exception e) {
            log.error("收集内存统计失败", e);
        }
    }

    /**
     * 收集命令统计
     */
    private void collectCommandStats() {
        try {
            Properties info = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("commandstats");

            for (String key : info.stringPropertyNames()) {
                String command = key.replace("cmdstat_", "");
                String value = info.getProperty(key);
                // 解析格式如：calls=123,usec=456,usec_per_call=3.70
                if (value != null) {
                    String[] parts = value.split(",");
                    for (String part : parts) {
                        if (part.startsWith("calls=")) {
                            try {
                                long calls = Long.parseLong(part.substring(6));
                                meterRegistry.gauge(monitorConfig.getMetricPrefix() + ".commands." + command,
                                        Tags.empty(), calls);
                            } catch (NumberFormatException e) {
                                log.warn("解析命令统计失败: command={}, value={}", command, value, e);
                            }
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("收集命令统计失败", e);
        }
    }

    /**
     * 检查告警
     */
    private void checkAlerts() {
        double memoryUsage = getMemoryUsage();
        if (memoryUsage > monitorConfig.getAlert().getMemoryUsageThreshold()) {
            log.warn("Redis内存使用率告警: {}% > {}%",
                    memoryUsage, monitorConfig.getAlert().getMemoryUsageThreshold());
        }

        int connections = getActiveConnections();
        // 这里需要获取最大连接数来计算使用率
        // 实际实现中应该从配置或Redis info中获取

        double errorRate = totalCommandCount.get() > 0 ?
                (double) failedCommandCount.get() / totalCommandCount.get() * 100 : 0;
        if (errorRate > monitorConfig.getAlert().getErrorRateThreshold()) {
            log.warn("Redis错误率告警: {}% > {}%",
                    errorRate, monitorConfig.getAlert().getErrorRateThreshold());
        }
    }

    /**
     * 获取监控统计信息
     */
    public MonitorStats getStats() {
        MonitorStats stats = new MonitorStats();
        stats.setTotalCommands(totalCommandCount.get());
        stats.setFailedCommands(failedCommandCount.get());
        stats.setSlowQueries(slowQueryCount.get());
        stats.setMemoryUsage(getMemoryUsage());
        stats.setActiveConnections(getActiveConnections());
        stats.setCommandStats(new ConcurrentHashMap<>(commandStats));
        return stats;
    }

    /**
     * 监控统计信息
     */
    @Data
    public static class MonitorStats {
        private long totalCommands;
        private long failedCommands;
        private long slowQueries;
        private double memoryUsage;
        private int activeConnections;
        private Map<String, AtomicLong> commandStats;

        public double getErrorRate() {
            return totalCommands > 0 ? (double) failedCommands / totalCommands * 100 : 0;
        }

        public double getSlowQueryRate() {
            return totalCommands > 0 ? (double) slowQueries / totalCommands * 100 : 0;
        }
    }
}