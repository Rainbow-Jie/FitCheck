@echo off
chcp 65001 >nul
echo ========================================
echo    FitCheck 健身打卡系统 - 后端启动脚本
echo ========================================
echo.

:: 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装JDK 8
    pause
    exit /b 1
)

:: 创建上传目录
if not exist "uploads\avatar" (
    mkdir uploads\avatar
    echo [创建] 上传目录已创建
)

:: 检查Maven
mvn -version >nul 2>&1
if %errorlevel% equ 0 (
    echo [检测] Maven已安装，开始构建项目...
    mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo [错误] Maven构建失败
        pause
        exit /b 1
    )
    echo [完成] 项目构建成功
    echo.
    echo 正在启动服务器...
    java -jar target\fitcheck-backend-1.0.0.jar
) else (
    echo [提示] 未检测到Maven，使用直接运行模式
    echo [提示] 请确保已编译项目，或安装Maven
    echo.
    echo 正在启动服务器...
    java -jar target\fitcheck-backend-1.0.0.jar
)

pause
