# 使用Arthas进行性能诊断

## 安装Arthas

```bash
# 下载Arthas
curl -O https://arthas.aliyun.com/arthas-boot.jar

# 或者使用在线安装
java -jar arthas-boot.jar
```

## 关键诊断命令

### 1. 监控方法执行时间
```bash
# 进入Arthas后，监控下单方法
watch cn.iocoder.boot.hmdianping.controller.voucher.VoucherOrderController createSeckillVoucherOrder '{params,returnObj,throwExp}' -x 3 -n 100

# 监控服务层方法
watch cn.iocoder.boot.hmdianping.service.voucher.Impl.VoucherOrderServiceImpl seckillVoucherLua '{params,returnObj,throwExp, #cost}' -x 3 -n 100
```

### 2. 追踪调用链
```bash
# 追踪整个调用链
trace cn.iocoder.boot.hmdianping.controller.voucher.VoucherOrderController createSeckillVoucherOrder -n 10

# 查看每个节点的耗时
trace *RateLimitHandler execute -n 20
trace *RedisIdWorker nextId -n 20
trace *VoucherOrderProducer sendSeckillOrderTransaction -n 20
```

### 3. 监控线程池
```bash
# 查看线程状态
thread

# 查看繁忙线程
thread -n 5

# 查看线程CPU使用
thread -i 1000 -n 5
```

### 4. 监控JVM
```bash
# 查看堆内存
dashboard

# 查看GC情况
jvm

# 查看类加载
classloader
```

### 5. 火焰图分析
```bash
# 生成CPU火焰图
profiler start
# 压测30秒
profiler stop --format html
```

## 关键指标关注点

### 方法耗时分布（预期）
- 限流Lua执行：< 5ms
- ID生成：< 1ms（雪花算法）
- RocketMQ发送：< 20ms
- 总耗时：< 30ms

### 如果发现瓶颈

#### 情况1：限流Lua耗时 > 10ms
**问题**：Redis成为瓶颈
**解决方案**：
1. 增加Redis实例
2. 使用Redis集群
3. 优化Lua脚本
4. 考虑本地限流

#### 情况2：RocketMQ发送耗时 > 50ms
**问题**：消息队列成为瓶颈
**解决方案**：
1. 优化RocketMQ配置
2. 使用异步发送
3. 批量发送消息
4. 增加Broker节点

#### 情况3：ID生成耗时 > 5ms
**问题**：ID生成器问题
**解决方案**：
1. 确认使用雪花算法
2. 检查机器ID冲突
3. 使用更高效的ID生成器

#### 情况4：线程阻塞
**问题**：线程池不足或锁竞争
**解决方案**：
1. 增加Tomcat线程数
2. 优化线程池配置
3. 减少同步锁使用

## 快速诊断脚本

```bash
#!/bin/bash
# arthas-quick-diagnose.sh

echo "1. 启动Arthas并附加到进程"
java -jar arthas-boot.jar

# 在Arthas中执行以下命令：
echo "2. 执行以下诊断命令:"
cat << 'EOF'
# 监控下单接口
watch cn.iocoder.boot.hmdianping.controller.voucher.VoucherOrderController createSeckillVoucherOrder '{params,returnObj,#cost}' -x 2 -n 50

# 查看线程状态
thread -n 10

# 查看方法调用链
trace cn.iocoder.boot.hmdianping.service.voucher.Impl.VoucherOrderServiceImpl seckillVoucherLua -n 20
EOF
```

## 常见问题排查

### Q1: 如何确定瓶颈在Redis？
```bash
# 在Arthas中
watch cn.iocoder.boot.framework.redis.core.ratelimit.RedisRateLimitHandler execute '{params,returnObj,#cost}' -x 2 -n 20

# 如果cost > 10ms，Redis是瓶颈
```

### Q2: 如何确定瓶颈在RocketMQ？
```bash
# 在Arthas中
watch cn.iocoder.boot.hmdianping.mq.producer.VoucherOrderProducer sendSeckillOrderTransaction '{params,returnObj,#cost}' -x 2 -n 20

# 如果cost > 50ms，RocketMQ是瓶颈
```

### Q3: 如何查看系统负载？
```bash
# 在Arthas中
dashboard

# 关注指标：
# 1. CPU使用率 > 80%
# 2. 内存使用率 > 80%
# 3. 线程数 > 200
# 4. GC频繁
```

## 优化建议

根据诊断结果：

### 如果Redis是瓶颈
1. **增加Redis实例**：使用Redis集群分散压力
2. **优化Lua脚本**：减少Redis操作次数
3. **本地限流**：使用Guava RateLimiter做第一层限流
4. **连接池优化**：进一步增加max-active

### 如果RocketMQ是瓶颈
1. **异步发送**：不等待消息确认
2. **批量发送**：合并消息批量发送
3. **增加Broker**：部署多个Broker节点
4. **优化配置**：调整发送超时和重试策略

### 如果应用服务器是瓶颈
1. **增加实例**：部署多个应用实例
2. **线程池优化**：调整Tomcat和业务线程池
3. **JVM调优**：优化GC参数和堆大小
4. **代码优化**：减少同步阻塞操作