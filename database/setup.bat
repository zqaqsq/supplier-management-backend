@echo off
echo ========================================
echo 供应商管理系统 - MySQL数据库设置脚本
echo ========================================
echo.

REM 检查MySQL是否安装
echo 正在检查MySQL安装...
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到MySQL命令，请确保MySQL已安装并添加到PATH环境变量
    echo 请访问 https://dev.mysql.com/downloads/mysql/ 下载并安装MySQL
    pause
    exit /b 1
)

echo MySQL已安装，版本信息：
mysql --version
echo.

REM 提示用户输入MySQL连接信息
set /p MYSQL_USER=请输入MySQL用户名 (默认: root): 
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASSWORD=请输入MySQL密码: 
if "%MYSQL_PASSWORD%"=="" (
    echo 错误：密码不能为空
    pause
    exit /b 1
)

set /p MYSQL_HOST=请输入MySQL主机地址 (默认: localhost): 
if "%MYSQL_HOST%"=="" set MYSQL_HOST=localhost

set /p MYSQL_PORT=请输入MySQL端口 (默认: 3306): 
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

echo.
echo 正在连接MySQL数据库...
echo 主机: %MYSQL_HOST%
echo 端口: %MYSQL_PORT%
echo 用户: %MYSQL_USER%
echo.

REM 测试MySQL连接
echo 正在测试数据库连接...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：无法连接到MySQL数据库，请检查：
    echo 1. MySQL服务是否启动
    echo 2. 用户名和密码是否正确
    echo 3. 主机地址和端口是否正确
    echo 4. 防火墙设置是否允许连接
    pause
    exit /b 1
)

echo 数据库连接成功！
echo.

REM 创建数据库
echo 正在创建数据库 supplier_management...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS supplier_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %errorlevel% neq 0 (
    echo 错误：创建数据库失败
    pause
    exit /b 1
)

echo 数据库创建成功！
echo.

REM 运行初始化脚本
echo 正在运行数据库初始化脚本...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% supplier_management < init.sql
if %errorlevel% neq 0 (
    echo 错误：运行初始化脚本失败
    pause
    exit /b 1
)

echo 数据库初始化完成！
echo.

REM 验证数据
echo 正在验证数据...
mysql -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASSWORD% supplier_management -e "SELECT 'supplier' as table_name, COUNT(*) as record_count FROM supplier UNION ALL SELECT 'graded_selection_rule' as table_name, COUNT(*) as record_count FROM graded_selection_rule UNION ALL SELECT 'selection_result' as table_name, COUNT(*) as record_count FROM selection_result UNION ALL SELECT 'operation_log' as table_name, COUNT(*) as record_count FROM operation_log;"

echo.
echo ========================================
echo 数据库设置完成！
echo ========================================
echo.
echo 下一步操作：
echo 1. 修改 src/main/resources/application.properties 中的数据库连接信息
echo 2. 启动Spring Boot应用程序
echo 3. 访问 http://localhost:8080 查看系统
echo.
echo 数据库连接信息：
echo URL: jdbc:mysql://%MYSQL_HOST%:%MYSQL_PORT%/supplier_management?useUnicode=true^&characterEncoding=utf8^&useSSL=false^&serverTimezone=GMT%%2B8^&allowPublicKeyRetrieval=true
echo 用户名: %MYSQL_USER%
echo 密码: %MYSQL_PASSWORD%
echo.
pause
