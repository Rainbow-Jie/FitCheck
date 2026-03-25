@echo off
chcp 65001 >nul
echo ========================================
echo    FitCheck 健身打卡系统 - 前端启动脚本
echo ========================================
echo.

:: 检查Node.js环境
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Node.js环境，请先安装Node.js
    pause
    exit /b 1
)

:: 安装依赖
echo [提示] 正在安装依赖...
call npm install
if %errorlevel% neq 0 (
    echo [错误] 依赖安装失败
    pause
    exit /b 1
)

echo [完成] 依赖安装成功
echo.
echo 正在启动开发服务器...
echo 访问地址: http://localhost:3000
echo.

:: 启动开发服务器
npm run dev

pause
