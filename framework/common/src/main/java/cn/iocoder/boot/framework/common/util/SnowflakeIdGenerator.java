package cn.iocoder.boot.framework.common.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


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

    // 序列号
    private long sequence = 0L;

    // 上一次时间戳
    private long lastTimestamp = -1L;

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
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒，获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
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