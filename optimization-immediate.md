# 立即优化方案

## 当前瓶颈分析

在去除限流后，1000 QPS的瓶颈可能来自：

### 1. RocketMQ事务消息（最可能）
- 同步发送，等待Broker响应
- 事务消息两阶段提交开销
- 网络往返延迟

### 2. 雪花算法锁竞争
- `synchronized`关键字在1000 QPS下可能成为瓶颈
- 每个请求都需要获取锁

### 3. 应用服务器配置
- Tomcat线程池可能不足
- JVM GC压力

## 立即优化措施

### 优化1：RocketMQ异步发送

**文件**: `hmdianping/src/main/java/cn/iocoder/boot/hmdianping/service/voucher/Impl/VoucherOrderServiceImpl.java`

```java
@Component
@Slf4j
public class AsyncOrderService {
    
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(50);
    private final VoucherOrderProducer voucherOrderProducer;
    
    public CompletableFuture<TransactionSendResult> sendSeckillOrderAsync(Long orderId, Long voucherId, Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return voucherOrderProducer.sendSeckillOrderTransaction(orderId, voucherId, userId);
            } catch (Exception e) {
                log.error("异步发送秒杀订单失败", e);
                throw new ServiceException(500, "秒杀失败");
            }
        }, asyncExecutor);
    }
}

// 修改seckillVoucherLua方法
public VoucherOrderDO seckillVoucherLua(Long voucherId) {
    Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
    Long orderId = redisIdWorker.nextId("voucher:order");
    
    // 异步发送
    CompletableFuture<TransactionSendResult> future = asyncOrderService.sendSeckillOrderAsync(orderId, voucherId, loginUserId);
    
    // 可以立即返回，或者等待一小段时间
    try {
        TransactionSendResult transactionSendResult = future.get(100, TimeUnit.MILLISECONDS);
        if (transactionSendResult.getLocalTransactionState() != LocalTransactionState.COMMIT_MESSAGE) {
            throw new ServiceException(500, "秒杀失败！");
        }
    } catch (TimeoutException e) {
        // 超时也认为成功，后台继续处理
        log.warn("消息发送超时，订单ID: {}", orderId);
    } catch (Exception e) {
        throw new ServiceException(500, "秒杀失败！");
    }
    
    return VoucherOrderDO.builder().voucherId(voucherId).id(orderId).userId(loginUserId).build();
}
```

### 优化2：无锁雪花算法

**文件**: `framework/common/src/main/java/cn/iocoder/boot/framework/common/util/LockFreeSnowflakeIdGenerator.java`

```java
@Component
public class LockFreeSnowflakeIdGenerator {
    
    private static final long START_TIMESTAMP = 1672531200000L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MACHINE_BITS = 5L;
    private static final long DATACENTER_BITS = 5L;
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    
    private final long datacenterId;
    private final long machineId;
    private final long machineIdShift;
    private final long datacenterIdShift;
    private final long timestampShift;
    
    private volatile long lastTimestamp = -1L;
    private final AtomicLong sequence = new AtomicLong(0L);
    
    public LockFreeSnowflakeIdGenerator(
            @Value("${snowflake.datacenter-id:1}") long datacenterId,
            @Value("${snowflake.machine-id:1}") long machineId) {
        
        this.datacenterId = datacenterId;
        this.machineId = machineId;
        this.machineIdShift = SEQUENCE_BITS;
        this.datacenterIdShift = SEQUENCE_BITS + MACHINE_BITS;
        this.timestampShift = SEQUENCE_BITS + MACHINE_BITS + DATACENTER_BITS;
    }
    
    public long nextId() {
        while (true) {
            long currentTimestamp = timeGen();
            long lastTimestamp = this.lastTimestamp;
            
            if (currentTimestamp < lastTimestamp) {
                throw new RuntimeException("Clock moved backwards");
            }
            
            if (currentTimestamp == lastTimestamp) {
                // 同一毫秒内，使用CAS递增序列号
                long currentSequence = sequence.get();
                long nextSequence = (currentSequence + 1) & MAX_SEQUENCE;
                
                if (nextSequence == 0) {
                    // 序列号溢出，等待下一毫秒
                    currentTimestamp = tilNextMillis(lastTimestamp);
                    this.lastTimestamp = currentTimestamp;
                    sequence.set(0L);
                } else if (sequence.compareAndSet(currentSequence, nextSequence)) {
                    return ((currentTimestamp - START_TIMESTAMP) << timestampShift)
                            | (datacenterId << datacenterIdShift)
                            | (machineId << machineIdShift)
                            | nextSequence;
                }
                // CAS失败，重试
            } else {
                // 新的毫秒，重置序列号
                this.lastTimestamp = currentTimestamp;
                sequence.set(0L);
                return ((currentTimestamp - START_TIMESTAMP) << timestampShift)
                        | (datacenterId << datacenterIdShift)
                        | (machineId << machineIdShift)
                        | 0L;
            }
        }
    }
    
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
```

### 优化3：Tomcat线程池优化

**文件**: `server/src/main/resources/application.yml`

```yaml
server:
  port: 8080
  tomcat:
    threads:
      max: 1000                 # 增加到1000
      min-spare: 200           # 最小空闲线程
    accept-count: 2000         # 等待队列长度
    max-connections: 10000     # 最大连接数
    connection-timeout: 10000  # 连接超时10秒
```

### 优化4：JVM参数优化

**启动参数**:
```bash
java -jar your-app.jar \
  -Xms2g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:InitiatingHeapOccupancyPercent=35 \
  -XX:+ParallelRefProcEnabled \
  -XX:MaxTenuringThreshold=1 \
  -Dserver.tomcat.max-threads=1000 \
  -Dserver.tomcat.accept-count=2000
```

## 优化实施步骤

### 第一步：诊断确认（1小时）
1. 使用Arthas确认具体瓶颈
2. 运行监控脚本收集数据
3. 确定主要瓶颈点

### 第二步：实施优化（2小时）
1. 实现无锁雪花算法
2. 实现RocketMQ异步发送
3. 优化Tomcat配置

### 第三步：测试验证（1小时）
1. 本地压测验证
2. 监控性能指标
3. 对比优化前后数据

### 第四步：生产部署（1小时）
1. 灰度发布
2. 监控告警
3. 回滚准备

## 预期效果

### 优化前（当前）
- QPS: 1000
- 响应时间: >100ms
- 瓶颈: RocketMQ/雪花算法锁

### 优化后（目标）
- QPS: 2000-3000
- 响应时间: <50ms
- 瓶颈: 网络/磁盘IO

## 风险控制

### 监控指标
1. **成功率**: >99.9%
2. **响应时间**: P95 < 100ms
3. **错误率**: <0.1%
4. **系统资源**: CPU < 80%, 内存 < 80%

### 回滚方案
1. 保留原有代码分支
2. 准备回滚脚本
3. 监控关键指标，异常时立即回滚

### 灰度发布
1. 先部署一个实例
2. 观察30分钟
3. 逐步扩大范围
4. 全量部署

## 紧急联系人
- 开发: [你的名字]
- 运维: [运维人员]
- 监控: [监控负责人]

## 后续优化
1. **Redis队列缓冲**: 使用Redis List作为缓冲队列
2. **批量处理**: 合并请求批量处理
3. **多实例部署**: 部署多个应用实例
4. **全链路监控**: 实现端到端监控