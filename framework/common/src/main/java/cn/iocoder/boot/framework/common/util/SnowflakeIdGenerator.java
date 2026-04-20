package cn.iocoder.boot.framework.common.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


/**
 * 雪花算法ID生成器
 * 结构：0 - 41位时间戳 - 5位数据中心ID - 5位机器ID - 12位序列号
 */
@Component
public class SnowflakeIdGenerator {

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
     * 修复后的版本：适配 AtomicLong 类型
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        // 1. 时钟回拨检查
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        // 2. 同一毫秒内的处理
        if (lastTimestamp == timestamp) {
            // 修复点：使用 getAndIncrement 或 addAndGet，然后手动赋值回 AtomicLong
            // 注意：AtomicLong 没有直接支持 & 操作的 setter，需要先计算再 set
            long nextSeq = (sequence.incrementAndGet()) & MAX_SEQUENCE;
            sequence.set(nextSeq);
            if (nextSeq == 0) {
                // 毫秒内溢出，阻塞到下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 3. 毫秒改变，重置序列号
            // 修复点：不能直接用 = 0L，必须用 .set()
            sequence.set(0L);
        }
        lastTimestamp = timestamp;
        // 4. 移位拼接 ID
        // 修复点：不能直接用 | sequence，必须用 .get() 获取 long 值
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence.get();
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
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
}