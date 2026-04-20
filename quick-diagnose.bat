@echo off
echo ==========================================
echo 快速瓶颈诊断脚本
echo ==========================================
echo.

echo 步骤1: 查找Java进程ID
echo ------------------------------------------
for /f "tokens=1" %%i in ('jps -l ^| findstr hmdp') do set PID=%%i
if defined PID (
    echo 找到Java进程: PID=%PID%
) else (
    echo 未找到Java进程，请确保应用正在运行
    pause
    exit /b 1
)
echo.

echo 步骤2: 查看线程状态
echo ------------------------------------------
echo 正在生成线程转储...
jstack %PID% > thread_dump_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%.txt
if %errorlevel% equ 0 (
    echo 线程转储生成成功
) else (
    echo 线程转储生成失败
)
echo.

echo 步骤3: 查看GC情况
echo ------------------------------------------
echo 正在收集GC信息（10秒）...
jstat -gc %PID% 1000 10
echo.

echo 步骤4: 查看系统资源
echo ------------------------------------------
echo CPU使用率:
wmic cpu get loadpercentage
echo.

echo 内存使用:
wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /format:value
echo.

echo 步骤5: 网络连接
echo ------------------------------------------
echo RocketMQ连接:
netstat -an | findstr "9876" | findstr "ESTABLISHED" | find /c "ESTABLISHED"
echo.

echo Redis连接:
netstat -an | findstr "6379" | findstr "ESTABLISHED" | find /c "ESTABLISHED"
echo.

echo 步骤6: 检查RocketMQ Topic
echo ------------------------------------------
where mqadmin >nul 2>nul
if %errorlevel% equ 0 (
    echo 检查Topic状态...
    mqadmin.cmd topicStatus -n 127.0.0.1:9876 -t topic_seckill_order
) else (
    echo mqadmin未找到，跳过Topic检查
)
echo.

echo ==========================================
echo 诊断完成
echo 建议:
echo 1. 查看线程转储文件中的阻塞线程
echo 2. 使用Arthas进行方法级耗时分析
echo 3. 检查RocketMQ Broker状态
echo ==========================================
pause