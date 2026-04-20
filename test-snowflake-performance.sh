#!/bin/bash

echo "=========================================="
echo "雪花算法性能测试脚本"
echo "=========================================="
echo ""

echo "测试目的：比较有锁和无锁版本的性能差异"
echo "测试环境：单机，模拟1000 QPS场景"
echo ""

# 创建测试Java类
cat > /tmp/TestSnowflakePerformance.java << 'EOF'
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class TestSnowflakePerformance {

    // 模拟有锁版本
    static class SynchronizedSnowflake {
        private long sequence = 0L;
        private long lastTimestamp = -1L;
        private static final long MAX_SEQUENCE = 4095; // 2^12 - 1

        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            if (timestamp < lastTimestamp) {
                throw new RuntimeException("Clock moved backwards");
            }
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }
            lastTimestamp = timestamp;
            return timestamp << 22 | sequence; // 简化版
        }

        private long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }

    // 模拟无锁版本
    static class LockFreeSnowflake {
        private final AtomicLong sequence = new AtomicLong(0L);
        private volatile long lastTimestamp = -1L;
        private static final long MAX_SEQUENCE = 4095;

        public long nextId() {
            while (true) {
                long currentTimestamp = System.currentTimeMillis();
                long lastTimestamp = this.lastTimestamp;

                if (currentTimestamp < lastTimestamp) {
                    throw new RuntimeException("Clock moved backwards");
                }

                if (currentTimestamp == lastTimestamp) {
                    long currentSequence = sequence.get();
                    long nextSequence = (currentSequence + 1) & MAX_SEQUENCE;

                    if (nextSequence == 0) {
                        currentTimestamp = tilNextMillis(lastTimestamp);
                        if (compareAndSetLastTimestamp(lastTimestamp, currentTimestamp)) {
                            sequence.set(0L);
                            return composeId(currentTimestamp, 0L);
                        }
                        continue;
                    }

                    if (sequence.compareAndSet(currentSequence, nextSequence)) {
                        return composeId(currentTimestamp, nextSequence);
                    }
                } else {
                    if (compareAndSetLastTimestamp(lastTimestamp, currentTimestamp)) {
                        sequence.set(0L);
                        return composeId(currentTimestamp, 0L);
                    }
                }
            }
        }

        private boolean compareAndSetLastTimestamp(long expect, long update) {
            // 简化实现
            if (this.lastTimestamp == expect) {
                this.lastTimestamp = update;
                return true;
            }
            return false;
        }

        private long composeId(long timestamp, long sequence) {
            return timestamp << 22 | sequence;
        }

        private long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                Thread.yield();
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }

    public static void main(String[] args) throws Exception {
        int threadCount = 50;
        int iterations = 20000; // 每个线程生成2万个ID
        int totalIds = threadCount * iterations;

        System.out.println("测试配置：");
        System.out.println("  线程数: " + threadCount);
        System.out.println("  每个线程生成ID数: " + iterations);
        System.out.println("  总ID数: " + totalIds);
        System.out.println();

        // 测试有锁版本
        System.out.println("=== 测试有锁版本 ===");
        testPerformance(new SynchronizedSnowflake(), threadCount, iterations, "有锁");

        // 测试无锁版本
        System.out.println("\n=== 测试无锁版本 ===");
        testPerformance(new LockFreeSnowflake(), threadCount, iterations, "无锁");
    }

    private static void testPerformance(Object generator, int threadCount, int iterations, String name)
            throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicLong errorCount = new AtomicLong(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        try {
                            if (generator instanceof SynchronizedSnowflake) {
                                ((SynchronizedSnowflake) generator).nextId();
                            } else {
                                ((LockFreeSnowflake) generator).nextId();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long totalIds = threadCount * iterations;

        System.out.println("  总耗时: " + totalTime + "ms");
        System.out.println("  QPS: " + (totalIds * 1000L / totalTime));
        System.out.println("  平均每个ID耗时: " + (totalTime * 1000000.0 / totalIds) + "ns");
        System.out.println("  错误数: " + errorCount.get());
    }
}
EOF

echo "编译测试程序..."
javac /tmp/TestSnowflakePerformance.java

if [ $? -eq 0 ]; then
    echo "编译成功，运行测试..."
    echo ""
    cd /tmp
    java TestSnowflakePerformance
else
    echo "编译失败，请检查Java环境"
fi

echo ""
echo "=========================================="
echo "测试说明："
echo "1. 有锁版本使用synchronized关键字"
echo "2. 无锁版本使用CAS操作"
echo "3. 测试模拟50个线程并发，每个线程生成2万个ID"
echo "4. 预期结果：无锁版本QPS应高于有锁版本"
echo "=========================================="