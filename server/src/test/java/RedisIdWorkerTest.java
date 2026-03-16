import cn.iocoder.boot.framework.redis.core.RedisIdWorker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
class RedisIdWorkerTest {

    @Resource
    private RedisIdWorker redisIdWorker;

    // 线程数（模拟 300 个并发用户）
    private static final int THREAD_COUNT = 300;
    // 每个线程生成的 ID 数量
    private static final int ID_PER_THREAD = 100;

    @Test
    void testNextId() throws InterruptedException {
        // 使用 CountDownLatch 确保所有线程同时开始执行
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // 用于存储所有生成的 ID，用来校验唯一性
        // 使用 ConcurrentHashMap 的 KeySet 保证线程安全且高效
        Set<Long> ids = ConcurrentHashMap.newKeySet();

        long begin = System.currentTimeMillis();

        // 定义任务
        Runnable task = () -> {
            try {
                for (int i = 0; i < ID_PER_THREAD; i++) {
                    long id = redisIdWorker.nextId("order");
                    ids.add(id);
                }
            } finally {
                // 每个线程执行完，计数器减 1
                latch.countDown();
            }
        };

        // 启动线程
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(task).start();
        }

        // 主线程阻塞，等待所有子线程执行完毕
        latch.await();

        long end = System.currentTimeMillis();

        // 结果统计
        System.out.println("================ 压测结果 ================");
        System.out.println("生成 ID 总数: " + ids.size());
        System.out.println("预期 ID 总数: " + (THREAD_COUNT * ID_PER_THREAD));
        System.out.println("总耗时 (ms): " + (end - begin));
        System.out.println("平均每秒生成数量 (TPS): " + (ids.size() * 1000L / (end - begin)));

        // 核心校验：唯一性检查
        Assertions.assertEquals(THREAD_COUNT * ID_PER_THREAD, ids.size(), "存在重复 ID！唯一性校验失败");
    }
}