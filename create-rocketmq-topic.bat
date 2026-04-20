@echo off
echo ============================================
echo RocketMQ Topic 创建脚本
echo ============================================
echo.

REM 设置RocketMQ安装目录（请根据实际安装位置修改）
set ROCKETMQ_HOME=C:\rocketmq

if not exist "%ROCKETMQ_HOME%" (
    echo 错误: RocketMQ目录不存在: %ROCKETMQ_HOME%
    echo 请修改脚本中的ROCKETMQ_HOME变量为你的RocketMQ安装目录
    pause
    exit /b 1
)

echo 正在创建Topic: topic_seckill_order
echo.

REM 切换到RocketMQ的bin目录
cd /d "%ROCKETMQ_HOME%\bin"

REM 创建Topic
call mqadmin.cmd updateTopic -n 127.0.0.1:9876 -c DefaultCluster -t topic_seckill_order -r 8 -w 8

if %errorlevel% equ 0 (
    echo.
    echo ============================================
    echo Topic创建成功!
    echo Topic名称: topic_seckill_order
    echo 读写队列数: 8
    echo ============================================
) else (
    echo.
    echo ============================================
    echo Topic创建失败!
    echo 请检查:
    echo 1. RocketMQ NameServer是否运行 (端口9876)
    echo 2. RocketMQ Broker是否运行
    echo 3. 是否有管理员权限
    echo ============================================
)

pause