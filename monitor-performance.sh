#!/bin/bash

echo "=========================================="
echo "性能瓶颈排查监控脚本"
echo "=========================================="
echo ""

# 1. 系统资源监控
echo "=== 系统资源 ==="
echo "CPU使用率:"
top -bn1 | grep "Cpu(s)" | awk '{print "User: "$2"% System: "$4"% Idle: "$8"%"}'
echo ""

echo "内存使用:"
free -h | grep -E "^(Mem:|Swap:)"
echo ""

# 2. Redis监控
echo "=== Redis监控 ==="
if command -v redis-cli &> /dev/null; then
    echo "Redis连接数:"
    redis-cli info clients | grep connected_clients
    echo ""

    echo "Redis内存使用:"
    redis-cli info memory | grep -E "used_memory_human|used_memory_peak_human"
    echo ""

    echo "Redis命令统计 (前10):"
    redis-cli info commandstats | grep -v "cmdstat_" | head -10
    echo ""

    echo "Redis慢查询 (最近10条):"
    redis-cli slowlog get 10 | head -20
else
    echo "Redis-cli未安装，跳过Redis监控"
fi
echo ""

# 3. Java进程监控
echo "=== Java进程监控 ==="
JAVA_PID=$(jps -l | grep hmdp | awk '{print $1}')
if [ -n "$JAVA_PID" ]; then
    echo "Java进程PID: $JAVA_PID"
    echo ""

    echo "线程数:"
    ps -T -p $JAVA_PID | wc -l
    echo ""

    echo "GC情况:"
    jstat -gc $JAVA_PID 1000 1 2>/dev/null || echo "无法获取GC信息"
else
    echo "未找到Java进程"
fi
echo ""

# 4. 网络监控
echo "=== 网络监控 ==="
echo "端口监听情况:"
netstat -an | grep -E "(6379|9876|8080)" | grep LISTEN
echo ""

echo "连接数统计:"
netstat -an | awk '/^tcp/ {print $6}' | sort | uniq -c | sort -rn
echo ""

echo "=========================================="
echo "监控完成"
echo "建议同时使用以下工具:"
echo "1. jvisualvm: Java可视化监控"
echo "2. arthas: Java诊断工具"
echo "3. Prometheus + Grafana: 系统监控"
echo "=========================================="