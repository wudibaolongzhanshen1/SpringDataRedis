# SpringDataRedis 项目

这是一个基于Spring Boot的Redis数据访问项目，包含多个模块。

## 项目结构

```
SpringDataRedis/
├── dependencies/           # 依赖管理模块
├── framework/              # 框架模块
│   ├── common/             # 通用工具和常量
│   │   ├── api/           # API接口定义
│   │   ├── collection/     # 集合工具
│   │   ├── dto/           # 数据传输对象
│   │   ├── enums/         # 枚举定义
│   │   ├── exception/     # 异常定义
│   │   ├── pojo/          # 普通Java对象
│   │   └── util/          # 工具类
│   ├── my-spring-boot-starter-cache/   # cache starter
│   │   ├── src/main/java/cn/iocoder/boot/framework/cache/
│   │   │   ├── config/     # 缓存配置
│   │   │   └── core/       # 缓存核心
│   ├── my-spring-boot-starter-job/   # job starter
│   │   ├── src/main/java/cn/iocoder/boot/framework/job/
│   │   │   ├── config/     # 任务配置
│   │   │   ├── core/       # 任务核心
│   │   │   │   ├── enums/  # 任务枚举
│   │   │   │   ├── handler/ # 任务处理器
│   │   │   │   ├── scheduler/ # 任务调度器
│   │   │   │   └── util/   # 任务工具
│   ├── my-spring-boot-starter-mq/   # mq starter
│   │   ├── src/main/java/cn/iocoder/boot/framework/mq/
│   │   │   ├── config/     # 消息队列配置
│   │   │   └── core/       # 消息队列核心
│   ├── my-spring-boot-starter-mybatis/   # mybatis starter
│   │   ├── src/main/java/cn/iocoder/boot/framework/mybatis/
│   │   │   ├── config/     # MyBatis配置
│   │   │   └── core/       # MyBatis核心
│   │   │       └── handler/ # MyBatis处理器
│   ├── my-spring-boot-starter-redis/   # redis starter
│   │   ├── src/main/java/cn/iocoder/boot/framework/redis/
│   │   │   ├── config/     # Redis配置
│   │   │   ├── core/       # Redis核心
│   │   │   │   ├── cache/  # 缓存相关
│   │   │   │   ├── listener/ # Redis监听器
│   │   │   │   ├── lua/    # Lua脚本
│   │   │   │   ├── ratelimit/ # 限流相关
│   │   │   │   ├── retry/  # 重试机制
│   │   │   │   └── util/   # Redis工具
│   │   │   ├── dao/        # Redis数据访问
│   │   │   ├── monitor/    # Redis监控
│   │   │   │   ├── annotation/ # 监控注解
│   │   │   │   └── aspect/ # 监控切面
│   │   │   ├── template/   # Redis模板
│   │   │   └── util/       # Redis工具
│   ├── my-spring-boot-starter-security/   # security starter
│   │   ├── src/main/java/cn/iocoder/boot/framework/security/
│   │   │   ├── config/     # 安全配置
│   │   │   └── core/       # 安全核心
│   │   │       ├── filter/ # 安全过滤器
│   │   │       └── util/   # 安全工具
│   └── my-spring-boot-starter-web/   # web starter
│       ├── src/main/java/cn/iocoder/boot/framework/web/
│       │   ├── config/     # Web配置
│       │   └── core/       # Web核心
│           ├── handler/    # 处理器
│           └── util/       # Web工具
├── hmdianping/             # 黑马点评业务模块
│   ├── src/main/java/cn/iocoder/boot/hmdianping/
│   │   ├── api/           # Api层
│   │   ├── canal/         # Canal数据同步
│   │   ├── controller/    # Controller层
│   │   ├── convert/       # 对象转换
│   │   ├── dal/           # Dal层
│   │   │   ├── dataobject/ # 数据对象
│   │   │   ├── mysql/     # MySQL数据访问
│   │   │   └── redis/     # Redis数据访问
│   │   ├── enums/         # 业务枚举
│   │   ├── framework/     # 业务框架
│   │   ├── job/           # 定时任务
│   │   ├── mq/            # 消息队列
│   │   ├── service/       # Service层
│   │   └── util/          # 业务工具
└── server/                 # 服务器模块
    ├── src/main/java/cn/iocoder/boot/server/
    └── src/main/resources/ # 配置文件

根目录重要文件:
├── .gitignore              # Git忽略配置
├── pom.xml                 # Maven配置
├── CLAUDE.md               # 项目文档
└── 异常处理分析报告.md      # 异常处理分析报告
```
SpringDataRedis/
├── dependencies/           # 依赖管理模块
├── framework/              # 框架模块
│   ├── common/             # 通用工具和常量
│   │   ├── api/
│   │   ├── collection/
│   │   ├── dto/
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── pojo/
│   │   ├── util/
│   ├── my-spring-boot-starter-cache/   # cache starter
│   ├── my-spring-boot-starter-job/   # job starter
│   ├── my-spring-boot-starter-mq/   # mq starter
│   ├── my-spring-boot-starter-mybatis/   # mybatis starter
│   ├── my-spring-boot-starter-redis/   # redis starter
│   ├── my-spring-boot-starter-security/   # security starter
│   ├── my-spring-boot-starter-web/   # web starter
├── hmdianping/             # 黑马点评业务模块
│   ├── api/           # Api层
│   ├── canal/
│   ├── controller/           # Controller层
│   ├── convert/
│   ├── dal/           # Dal层
│   ├── enums/
│   ├── framework/
│   ├── job/
│   ├── mq/
│   ├── service/           # Service层
└── server/                 # 服务器模块

根目录重要文件:
├── .gitignore              # Git忽略配置
├── pom.xml                 # Maven配置
├── CLAUDE.md               # 项目文档
├── 异常处理分析报告.md      # 异常处理分析报告
```

## 模块说明

### 1. dependencies (依赖管理)
- 统一管理项目依赖版本

### 2. framework (框架模块)
- **common**: 通用工具类、异常定义、枚举等
- **my-spring-boot-starter-***: 各种Spring Boot Starter
  - **redis**: Redis操作封装，包含缓存、分布式锁、限流等
  - **web**: Web相关配置，全局异常处理等
  - **security**: 安全认证授权
  - **mybatis**: MyBatis集成
  - **job**: 定时任务调度
  - **mq**: 消息队列集成

### 3. hmdianping (业务模块)
- 黑马点评业务实现
- 包含用户、优惠券、博客、关注等业务功能
- 使用Redis进行缓存、分布式锁、限流等

### 4. server (服务器模块)
- 应用启动入口
- 主配置类

## 关键技术栈

- **Java 17**: 编程语言
- **Spring Boot 3.x**: 应用框架
- **Redis**: 缓存、分布式锁、限流
- **MyBatis**: 数据访问
- **RocketMQ**: 消息队列
- **Quartz**: 定时任务调度

## 重要文件位置

### 配置类
- `framework/my-spring-boot-starter-redis/src/main/java/cn/iocoder/boot/framework/redis/config/RedisAutoConfiguration.java` - Redis自动配置
- `framework/my-spring-boot-starter-web/src/main/java/cn/iocoder/boot/framework/web/config/MyWebAutoConfiguration.java` - Web自动配置

### 核心工具类
- `framework/common/src/main/java/cn/iocoder/boot/framework/common/util/JsonUtils.java` - JSON工具
- `framework/my-spring-boot-starter-redis/src/main/java/cn/iocoder/boot/framework/redis/core/CacheClient.java` - 缓存客户端
- `framework/my-spring-boot-starter-redis/src/main/java/cn/iocoder/boot/framework/redis/core/RedisIdWorker.java` - Redis ID生成器

### 异常处理
- `framework/my-spring-boot-starter-web/src/main/java/cn/iocoder/boot/framework/web/core/handler/GlobalExceptionHandler.java` - 全局异常处理器
- `framework/common/src/main/java/cn/iocoder/boot/framework/common/exception/ServiceException.java` - 业务异常

### 业务代码
- `hmdianping/src/main/java/cn/iocoder/boot/hmdianping/service/voucher/` - 优惠券服务
- `hmdianping/src/main/java/cn/iocoder/boot/hmdianping/controller/auth/` - 认证控制器

## 自定义命令

项目包含自定义Claude Code命令：

### analysisbug
分析项目中的潜在bug并格式化输出。

```bash
# 执行所有检查
/analysisbug

# 只检查异常处理问题
/analysisbug -e

# 查看帮助
/analysisbug --help
```

### update-directory-structure
更新项目目录结构到CLAUDE.md文件。

```bash
/update-directory-structure
```

## 开发规范

1. **异常处理**: 使用自定义异常，通过GlobalExceptionHandler统一处理
2. **返回格式**: 使用CommonResult统一返回格式
3. **缓存操作**: 通过CacheClient进行缓存操作，防止缓存击穿、穿透
4. **ID生成**: 使用RedisIdWorker生成分布式ID
5. **限流**: 使用RedisRateLimitHandler进行接口限流

## 更新记录

- **2026-04-13**: 创建项目目录结构文档
- **2026-04-13**: 添加analysisbug自定义命令
- **2026-04-13**: 添加update-directory-structure自定义技能
- **2026-04-13**: 生成异常处理分析报告

---

*最后更新: 2026-04-13 15:24:00
*使用 `/update-directory-structure` 命令更新目录结构*
