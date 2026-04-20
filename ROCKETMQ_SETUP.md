# RocketMQ 配置说明

## 问题描述
应用启动后，秒杀下单接口报错：
```
org.apache.rocketmq.client.exception.MQClientException: No route info of this topic: topic_seckill_order
```

## 原因
RocketMQ中还没有创建 `topic_seckill_order` 这个Topic。

## 解决方案

### 方案1：手动创建Topic（推荐）

#### Windows 系统
1. 确保RocketMQ已安装并运行
2. 打开命令提示符（管理员权限）
3. 运行以下命令：
```bash
cd C:\rocketmq\bin
mqadmin.cmd updateTopic -n 127.0.0.1:9876 -c DefaultCluster -t topic_seckill_order -r 8 -w 8
```

#### Linux/Mac 系统
```bash
cd /opt/rocketmq/bin
./mqadmin updateTopic -n 127.0.0.1:9876 -c DefaultCluster -t topic_seckill_order -r 8 -w 8
```

#### 使用提供的脚本
项目根目录下提供了创建脚本：
- Windows: `create-rocketmq-topic.bat`
- Linux/Mac: `create-rocketmq-topic.sh`

**注意**：需要修改脚本中的 `ROCKETMQ_HOME` 变量为你的RocketMQ安装目录。

### 方案2：配置自动创建Topic

已配置 `autoCreateTopicEnable = true`，但需要确保：
1. RocketMQ Broker配置允许自动创建Topic
2. 在Broker的配置文件中添加：
```properties
autoCreateTopicEnable=true
```

### 方案3：使用RocketMQ控制台
1. 访问RocketMQ控制台（如果有部署）
2. 在Topic管理页面创建新Topic
3. Topic名称：`topic_seckill_order`
4. 读写队列数：8

## 验证Topic是否创建成功

### 方法1：使用命令行
```bash
# Windows
mqadmin.cmd topicList -n 127.0.0.1:9876

# Linux/Mac
./mqadmin topicList -n 127.0.0.1:9876
```

### 方法2：查看Broker日志
检查RocketMQ Broker的日志文件，搜索 `topic_seckill_order`。

### 方法3：使用RocketMQ控制台
在控制台的Topic列表中查看是否存在 `topic_seckill_order`。

## 配置说明

### 已配置的参数
- NameServer地址：`127.0.0.1:9876`
- 生产者组：`GID_SECKILL_PRODUCER`
- 消费者组：`g_hmdp_consumer`
- 自动创建Topic：已启用
- 发送超时：2000ms
- 最大消息大小：4MB

### 性能优化配置
- Redis连接池：max-active=500
- ID生成器：使用雪花算法（本地生成）
- RocketMQ连接池：优化线程配置

## 重启应用
创建Topic后，需要重启Spring Boot应用。

## 故障排除

### 1. Topic创建失败
- 检查RocketMQ NameServer是否运行：`netstat -an | grep 9876`
- 检查RocketMQ Broker是否运行
- 检查是否有权限创建Topic

### 2. 自动创建Topic不生效
- 检查Broker配置 `autoCreateTopicEnable=true`
- 检查应用配置中的 `autoCreateTopicEnable` 参数
- 可能需要重启Broker

### 3. 消息发送失败
- 检查网络连接
- 检查防火墙设置
- 查看RocketMQ日志

## 联系支持
如果问题仍然存在，请检查：
1. RocketMQ版本是否兼容
2. 网络配置是否正确
3. 系统资源是否充足