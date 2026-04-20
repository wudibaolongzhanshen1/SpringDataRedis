package cn.iocoder.boot.framework.common.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * 雪花算法ID生成器（无锁实现）
 * 结构：0 - 41位时间戳 - 5位数据中心ID - 5位机器ID - 12位序列号
 * 使用CAS操作代替synchronized，消除锁竞争，适合高并发场景
 */
@Component
public class SnowflakeIdGeneratorLockFree {

    // 起始时间戳（2023-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1672531200000L;

    // 每部分占用的位数
    private static final long SEQUENCE_BITS = 12L; // 序列号占用的位数
    private static final long MACHINE_BITS = 5L;   // 机器标识占用的位数
    private static final long DATACENTER_BITS = 5L; // 数据中心占用的位数

    // 每部分的最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BITS);
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BITS);

    // 每部分向左的位移
    private static final long MACHINE_LEFT = SEQUENCE_BITS;
    private static final long DATACENTER_LEFT = SEQUENCE_BITS + MACHINE_BITS;
    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BITS;

    // 数据中心ID
    @Value("${snowflake.datacenter-id:1}")
    private long datacenterId;

    // 机器ID
    @Value("${snowflake.machine-id:1}")
    private long machineId;

    // 序列号 - 使用AtomicLong保证原子性
    private final AtomicLong sequence = new AtomicLong(0L);

    // 上一次时间戳 - 使用volatile保证可见性
    private volatile long lastTimestamp = -1L;

    // 用于CAS更新lastTimestamp
    private static final AtomicLongFieldUpdater<SnowflakeIdGeneratorLockFree> LAST_TIMESTAMP_UPDATER =
            AtomicLongFieldUpdater.newUpdater(SnowflakeIdGeneratorLockFree.class, "lastTimestamp");

    @PostConstruct
    public void init() {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
    }

    /**
     * 生成下一个ID（无锁实现）
     * 使用CAS操作避免锁竞争，适合高并发场景
     *
     * 算法流程：
     * 1. 获取当前时间戳
     * 2. 检查时钟回退
     * 3. 如果同一毫秒：CAS递增序列号
     * 4. 如果新毫秒：CAS更新时间戳并重置序列号
     * 5. 组合ID各部分返回
     */
    public long nextId() {
        while (true) {
            long currentTimestamp = timeGen();
            long lastTimestamp = this.lastTimestamp;
            // 检查时钟回退
            if (currentTimestamp < lastTimestamp) {
                throw new RuntimeException("Clock moved backwards. Refusing to generate id");
            }
            if (currentTimestamp == lastTimestamp) {
                // 同一毫秒内，使用CAS递增序列号
                long currentSequence = sequence.get();
                long nextSequence = (currentSequence + 1) & MAX_SEQUENCE;
                if (nextSequence == 0) {
                    // 序列号溢出，等待下一毫秒
                    currentTimestamp = tilNextMillis(lastTimestamp);
                    // 更新时间戳并重置序列号
                    if (LAST_TIMESTAMP_UPDATER.compareAndSet(this, lastTimestamp, currentTimestamp)) {
                        sequence.set(0L);
                        return composeId(currentTimestamp, 0L);
                    }
                    // CAS失败，重试
                    continue;
                }
                // 尝试CAS更新序列号
                if (sequence.compareAndSet(currentSequence, nextSequence)) {
                    return composeId(currentTimestamp, nextSequence);
                }
                // CAS失败，重试
            } else {
                // 新的毫秒，重置序列号为0，并更新时间戳
                if (LAST_TIMESTAMP_UPDATER.compareAndSet(this, lastTimestamp, currentTimestamp)) {
                    sequence.set(0L);
                    return composeId(currentTimestamp, 0L);
                }
                // CAS失败，重试
            }
        }
    }

    /**
     * 组合ID各部分
     */
    private long composeId(long timestamp, long sequence) {
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * 使用Thread.yield()避免CPU空转
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            // 短暂让出CPU，避免空转消耗
            Thread.yield();
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回当前时间，以毫秒为单位
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 获取数据中心ID
     */
    public long getDatacenterId() {
        return datacenterId;
    }

    /**
     * 获取机器ID
     */
    public long getMachineId() {
        return machineId;
    }

    /**
     * 获取起始时间戳
     */
    public static long getStartTimestamp() {
        return START_TIMESTAMP;
    }

    /**
     * 获取当前序列号（用于测试和监控）
     */
    public long getCurrentSequence() {
        return sequence.get();
    }

    /**
     * 获取最后时间戳（用于测试和监控）
     */
    public long getLastTimestamp() {
        return lastTimestamp;
    }

    /**
     * 性能测试方法（用于验证无锁效果）
     */
    public void performanceTest(int iterations) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            nextId();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("生成 " + iterations + " 个ID耗时: " + (endTime - startTime) + "ms");
        System.out.println("平均每个ID耗时: " + ((endTime - startTime) * 1000000.0 / iterations) + "ns");
    }
}