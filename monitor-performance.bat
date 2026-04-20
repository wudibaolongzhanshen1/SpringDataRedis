@echo off
echo ==========================================
echo 性能瓶颈排查监控脚本
echo ==========================================
echo.

REM 1. 系统资源监控
echo === 系统资源 ===
echo CPU和内存使用:
wmic cpu get loadpercentage
wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /format:value
echo.

REM 2. Redis监控
echo === Redis监控 ===
where redis-cli >nul 2>nul
if %errorlevel% equ 0 (
    echo Redis连接数:
    redis-cli info clients | findstr "connected_clients"
    echo.

    echo Redis内存使用:
    redis-cli info memory | findstr "used_memory_human used_memory_peak_human"
    echo.

    echo Redis命令统计:
    redis-cli info commandstats | findstr /v "cmdstat_" | head -10
    echo.

    echo Redis慢查询:
    redis-cli slowlog get 10 | head -20
) else (
    echo Redis-cli未找到，跳过Redis监控
)
echo.

REM 3. Java进程监控
echo === Java进程监控 ===
for /f "tokens=1" %%i in ('jps -l ^| findstr hmdp') do set JAVA_PID=%%i
if defined JAVA_PID (
    echo Java进程PID: %JAVA_PID%
    echo.

    echo 线程数:
    jstack %JAVA_PID% 2>nul | find /c "nid=" || echo 无法获取线程信息
    echo.

    echo 建议使用jvisualvm或arthas进行详细监控
) else (
    echo 未找到Java进程
)
echo.

REM 4. 网络监控
echo === 网络监控 ===
echo 端口监听情况:
netstat -an | findstr "6379 9876 8080" | findstr "LISTENING"
echo.

echo 连接数统计:
netstat -an | findstr "TCP" | awk "{print $6}" | sort | uniq -c | sort -rn
echo.

echo ==========================================
echo 监控完成
echo 建议同时使用以下工具:
echo 1. jvisualvm: Java可视化监控
echo 2. arthas: Java诊断工具
echo 3. Prometheus + Grafana: 系统监控
echo ==========================================
pause