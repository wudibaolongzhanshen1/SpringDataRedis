package cn.iocoder.boot.framework.redis.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

/**
 * Redis健康检查指示器
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.redis.monitor.enabled", havingValue = "true", matchIfMissing = false)
public class RedisHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisMonitorConfig monitorConfig;

    // 健康状态缓存
    private volatile Health lastHealth = Health.unknown().build();
    private volatile long lastCheckTime = 0;
    private static final long HEALTH_CACHE_DURATION = 5000; // 5秒缓存

    public RedisHealthIndicator(StringRedisTemplate stringRedisTemplate,
                               RedisMonitorConfig monitorConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.monitorConfig = monitorConfig;
    }

    @Override
    public Health health() {
        if (!monitorConfig.isHealthCheckEnabled()) {
            return Health.up().withDetail("enabled", false).build();
        }

        // 检查缓存
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < HEALTH_CACHE_DURATION) {
            return lastHealth;
        }

        try {
            Health health = checkHealth();
            lastHealth = health;
            lastCheckTime = now;
            return health;
        } catch (Exception e) {
            Health health = Health.down(e).build();
            lastHealth = health;
            lastCheckTime = now;
            return health;
        }
    }

    /**
     * 执行健康检查
     */
    private Health checkHealth() {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 检查连接
            String pong = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            if (!"PONG".equals(pong)) {
                return Health.down()
                        .withDetail("error", "Ping响应异常: " + pong)
                        .withDetail("responseTime", System.currentTimeMillis() - startTime)
                        .build();
            }

            long pingTime = System.currentTimeMillis() - startTime;

            // 2. 检查内存使用率
            Properties memoryInfo = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("memory");

            String usedMemory = memoryInfo.getProperty("used_memory");
            String maxMemory = memoryInfo.getProperty("maxmemory");
            double memoryUsage = 0.0;

            if (usedMemory != null && maxMemory != null && !"0".equals(maxMemory)) {
                double used = Double.parseDouble(usedMemory);
                double max = Double.parseDouble(maxMemory);
                memoryUsage = (used / max) * 100;
            }

            // 3. 检查连接数
            Properties clientInfo = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("clients");
            int connectedClients = clientInfo.getProperty("connected_clients") != null ?
                    Integer.parseInt(clientInfo.getProperty("connected_clients")) : 0;

            // 4. 检查持久化（如果配置了）
            Properties persistenceInfo = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("persistence");
            String rdbLastSaveStatus = persistenceInfo.getProperty("rdb_last_bgsave_status");
            String aofLastWriteStatus = persistenceInfo.getProperty("aof_last_write_status");

            // 构建健康状态
            Health.Builder healthBuilder = Health.up()
                    .withDetail("ping", pong)
                    .withDetail("responseTime", pingTime + "ms")
                    .withDetail("connectedClients", connectedClients);

            if (usedMemory != null) {
                healthBuilder.withDetail("usedMemory", usedMemory + " bytes");
            }
            if (maxMemory != null && !"0".equals(maxMemory)) {
                healthBuilder.withDetail("maxMemory", maxMemory + " bytes")
                        .withDetail("memoryUsage", String.format("%.2f%%", memoryUsage));
            }
            if (rdbLastSaveStatus != null) {
                healthBuilder.withDetail("rdbLastSaveStatus", rdbLastSaveStatus);
            }
            if (aofLastWriteStatus != null) {
                healthBuilder.withDetail("aofLastWriteStatus", aofLastWriteStatus);
            }

            // 5. 检查告警条件
            if (memoryUsage > monitorConfig.getAlert().getMemoryUsageThreshold()) {
                healthBuilder.withDetail("memoryWarning",
                        String.format("内存使用率过高: %.2f%%", memoryUsage));
            }

            // 检查持久化状态
            if ("err".equals(rdbLastSaveStatus)) {
                healthBuilder.withDetail("rdbWarning", "RDB持久化失败");
            }
            if ("err".equals(aofLastWriteStatus)) {
                healthBuilder.withDetail("aofWarning", "AOF持久化失败");
            }

            return healthBuilder.build();

        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            return Health.down(e)
                    .withDetail("error", e.getMessage())
                    .withDetail("responseTime", System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * 强制刷新健康状态
     */
    public Health refreshHealth() {
        lastCheckTime = 0;
        return health();
    }

    /**
     * 获取详细健康信息
     */
    public Health getDetailedHealth() {
        Health basicHealth = health();
        if (!basicHealth.getStatus().equals(Health.up().build().getStatus())) {
            return basicHealth;
        }

        try {
            // 获取更多详细信息
            Properties statsInfo = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("stats");
            Properties cpuInfo = stringRedisTemplate.getConnectionFactory()
                    .getConnection()
                    .info("cpu");

            Health.Builder detailedBuilder = Health.up();

            // 添加统计信息
            if (statsInfo != null) {
                detailedBuilder.withDetail("totalConnectionsReceived",
                        statsInfo.get("total_connections_received"))
                        .withDetail("totalCommandsProcessed",
                                statsInfo.get("total_commands_processed"))
                        .withDetail("instantaneousOpsPerSec",
                                statsInfo.get("instantaneous_ops_per_sec"));
            }

            // 添加CPU信息
            if (cpuInfo != null) {
                detailedBuilder.withDetail("usedCpuSys", cpuInfo.get("used_cpu_sys"))
                        .withDetail("usedCpuUser", cpuInfo.get("used_cpu_user"));
            }

            return detailedBuilder.build();

        } catch (Exception e) {
            log.error("获取详细健康信息失败", e);
            return basicHealth;
        }
    }
}