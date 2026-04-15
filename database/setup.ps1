# 供应商管理系统 - MySQL数据库设置脚本 (PowerShell版本)
Write-Host "========================================" -ForegroundColor Green
Write-Host "供应商管理系统 - MySQL数据库设置脚本" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 检查MySQL是否安装
Write-Host "正在检查MySQL安装..." -ForegroundColor Yellow
try {
    $mysqlVersion = mysql --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "MySQL已安装，版本信息：" -ForegroundColor Green
        Write-Host $mysqlVersion -ForegroundColor Cyan
    } else {
        throw "MySQL未找到"
    }
} catch {
    Write-Host "错误：未找到MySQL命令，请确保MySQL已安装并添加到PATH环境变量" -ForegroundColor Red
    Write-Host "请访问 https://dev.mysql.com/downloads/mysql/ 下载并安装MySQL" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

Write-Host ""

# 提示用户输入MySQL连接信息
$MYSQL_USER = Read-Host "请输入MySQL用户名 (默认: root)"
if ([string]::IsNullOrEmpty($MYSQL_USER)) { $MYSQL_USER = "root" }

$MYSQL_PASSWORD = Read-Host "请输入MySQL密码" -AsSecureString
$MYSQL_PASSWORD_PLAIN = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($MYSQL_PASSWORD))
if ([string]::IsNullOrEmpty($MYSQL_PASSWORD_PLAIN)) {
    Write-Host "错误：密码不能为空" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

$MYSQL_HOST = Read-Host "请输入MySQL主机地址 (默认: localhost)"
if ([string]::IsNullOrEmpty($MYSQL_HOST)) { $MYSQL_HOST = "localhost" }

$MYSQL_PORT = Read-Host "请输入MySQL端口 (默认: 3306)"
if ([string]::IsNullOrEmpty($MYSQL_PORT)) { $MYSQL_PORT = "3306" }

Write-Host ""
Write-Host "正在连接MySQL数据库..." -ForegroundColor Yellow
Write-Host "主机: $MYSQL_HOST" -ForegroundColor Cyan
Write-Host "端口: $MYSQL_PORT" -ForegroundColor Cyan
Write-Host "用户: $MYSQL_USER" -ForegroundColor Cyan
Write-Host ""

# 测试MySQL连接
Write-Host "正在测试数据库连接..." -ForegroundColor Yellow
$testConnection = mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD_PLAIN -e "SELECT 1;" 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误：无法连接到MySQL数据库，请检查：" -ForegroundColor Red
    Write-Host "1. MySQL服务是否启动" -ForegroundColor Red
    Write-Host "2. 用户名和密码是否正确" -ForegroundColor Red
    Write-Host "3. 主机地址和端口是否正确" -ForegroundColor Red
    Write-Host "4. 防火墙设置是否允许连接" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

Write-Host "数据库连接成功！" -ForegroundColor Green
Write-Host ""

# 创建数据库
Write-Host "正在创建数据库 supplier_management..." -ForegroundColor Yellow
$createDb = mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD_PLAIN -e "CREATE DATABASE IF NOT EXISTS supplier_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误：创建数据库失败" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

Write-Host "数据库创建成功！" -ForegroundColor Green
Write-Host ""

# 运行初始化脚本
Write-Host "正在运行数据库初始化脚本..." -ForegroundColor Yellow
$initScript = mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD_PLAIN supplier_management < init.sql
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误：运行初始化脚本失败" -ForegroundColor Red
    Read-Host "按任意键退出"
    exit 1
}

Write-Host "数据库初始化完成！" -ForegroundColor Green
Write-Host ""

# 验证数据
Write-Host "正在验证数据..." -ForegroundColor Yellow
mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD_PLAIN supplier_management -e "SELECT 'supplier' as table_name, COUNT(*) as record_count FROM supplier UNION ALL SELECT 'graded_selection_rule' as table_name, COUNT(*) as record_count FROM graded_selection_rule UNION ALL SELECT 'selection_result' as table_name, COUNT(*) as record_count FROM selection_result UNION ALL SELECT 'operation_log' as table_name, COUNT(*) as record_count FROM operation_log;"

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "数据库设置完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 修改 src/main/resources/application.properties 中的数据库连接信息" -ForegroundColor White
Write-Host "2. 启动Spring Boot应用程序" -ForegroundColor White
Write-Host "3. 访问 http://localhost:8080 查看系统" -ForegroundColor White
Write-Host ""
Write-Host "数据库连接信息：" -ForegroundColor Yellow
Write-Host "URL: jdbc:mysql://$MYSQL_HOST`:$MYSQL_PORT/supplier_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true" -ForegroundColor Cyan
Write-Host "用户名: $MYSQL_USER" -ForegroundColor Cyan
Write-Host "密码: $MYSQL_PASSWORD_PLAIN" -ForegroundColor Cyan
Write-Host ""

# 清理密码变量
$MYSQL_PASSWORD_PLAIN = $null

Read-Host "按任意键退出"
