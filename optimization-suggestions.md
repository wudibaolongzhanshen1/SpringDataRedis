# 性能优化建议

## 1. 限流策略优化（最有效）

### 当前问题
每个请求都执行Redis Lua脚本，在1000 QPS下：
- 1000次/秒 Redis操作
- Lua脚本包含多个Redis命令
- 网络往返延迟

### 优化方案：分级限流

#### 第一层：本地限流（Guava RateLimiter）
```java
// 在Controller或Service中添加
private final RateLimiter localRateLimiter = RateLimiter.create(2000); // 2000 QPS

@PostMapping("/createSeckillVoucherOrder")
public void createSeckillVoucherOrder(Long voucherId) throws Exception {
    // 本地限流
    if (!localRateLimiter.tryAcquire()) {
        throw new ServiceException(500, "系统繁忙，请稍后重试");
    }
    
    // Redis限流
    rateLimitHandler.execute(voucherId, SecurityFrameworkUtils.getLoginUserId(), RateLimitScene.SECKILL_ORDER);
    voucherOrderService.seckillVoucherLua(voucherId);
}
```

#### 第二层：Redis限流（现有）
- 保持现有Redis限流逻辑
- 本地限流会过滤掉大部分请求，减轻Redis压力

#### 第三层：分布式限流（可选）
- 使用Redis Cluster分散压力
- 使用Sentinel进行熔断降级

## 2. RocketMQ异步优化

### 当前问题
同步发送事务消息，等待Broker响应

### 优化方案：异步发送 + 本地存储

```java
@Component
@Slf4j
public class AsyncVoucherOrderProducer {
    
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(20);
    private final VoucherOrderProducer voucherOrderProducer;
    
    public void sendSeckillOrderAsync(Long orderId, Long voucherId, Long userId) {
        asyncExecutor.submit(() -> {
            try {
                voucherOrderProducer.sendSeckillOrderTransaction(orderId, voucherId, userId);
            } catch (Exception e) {
                log.error("异步发送秒杀订单失败", e);
                // 可以存储到本地队列重试
            }
        });
    }
}
```

## 3. 批量处理优化

### 批量ID生成
```java
@Component
public class BatchIdGenerator {
    
    private final SnowflakeIdGenerator idGenerator;
    private final BlockingQueue<Long> idQueue = new LinkedBlockingQueue<>(1000);
    
    @PostConstruct
    public void init() {
        // 预生成ID
        new Thread(() -> {
            while (true) {
                try {
                    if (idQueue.size() < 500) {
                        for (int i = 0; i < 100; i++) {
                            idQueue.put(idGenerator.nextId());
                        }
                    }
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
    
    public Long nextId() {
        try {
            return idQueue.take();
        } catch (InterruptedException e) {
            return idGenerator.nextId();
        }
    }
}
```

## 4. 连接池优化补充

### Redis连接池进一步优化
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 1000      # 增加到1000
          max-idle: 200
          min-idle: 50
          max-wait: 500ms       # 减少等待时间
          time-between-eviction-runs: 30000ms
```

### Tomcat连接池优化
```yaml
server:
  tomcat:
    threads:
      max: 500                 # 最大线程数
      min-spare: 50            # 最小空闲线程
    accept-count: 1000         # 等待队列长度
    max-connections: 10000     # 最大连接数
```

## 5. 监控和告警

### 关键监控指标
1. **Redis**：
   - 连接数使用率
   - 命令执行时间
   - 内存使用率
   - CPU使用率

2. **RocketMQ**：
   - 消息发送耗时
   - 消息堆积量
   - Broker CPU/内存

3. **应用**：
   - 接口响应时间
   - 错误率
   - JVM GC频率
   - 线程池使用率

### 告警阈值
- Redis命令执行时间 > 10ms：告警
- RocketMQ发送耗时 > 50ms：告警
- 接口错误率 > 1%：告警
- JVM Full GC频率 > 1次/分钟：告警

## 6. 压测建议

### 压测场景
1. **基准测试**：1000 QPS，持续5分钟
2. **峰值测试**：2000 QPS，持续2分钟
3. **稳定性测试**：1500 QPS，持续30分钟
4. **破坏性测试**：3000 QPS，直到系统崩溃

### 监控指标
```bash
# 压测时监控
./monitor-performance.sh  # 每10秒执行一次

# 使用wrk进行压测
wrk -t20 -c1000 -d60s --latency http://localhost:8080/voucherOrder/createSeckillVoucherOrder?voucherId=1
```

## 7. 紧急优化清单

### 立即实施（1小时内）
1. 添加本地限流（Guava RateLimiter）
2. 增加Redis连接池到1000
3. 优化Tomcat线程池配置

### 短期优化（1天内）
1. 实现RocketMQ异步发送
2. 添加批量ID生成
3. 部署监控告警

### 长期优化（1周内）
1. Redis集群部署
2. RocketMQ集群部署
3. 应用多实例部署
4. 全链路监控

## 预期效果

### 优化后目标
- **QPS**：从1000提升到2000-3000
- **响应时间**：从>100ms降低到<50ms
- **稳定性**：支持持续高并发

### 风险控制
1. **灰度发布**：先优化一个实例
2. **回滚方案**：准备好回滚脚本
3. **监控告警**：实时监控关键指标
4. **压测验证**：优化后必须压测验证