#!/bin/bash

echo "========================================"
echo "供应商管理系统 - MySQL数据库设置脚本"
echo "========================================"
echo

# 检查MySQL是否安装
echo "正在检查MySQL安装..."
if ! command -v mysql &> /dev/null; then
    echo "错误：未找到MySQL命令，请确保MySQL已安装并添加到PATH环境变量"
    echo "请访问 https://dev.mysql.com/downloads/mysql/ 下载并安装MySQL"
    exit 1
fi

echo "MySQL已安装，版本信息："
mysql --version
echo

# 提示用户输入MySQL连接信息
read -p "请输入MySQL用户名 (默认: root): " MYSQL_USER
MYSQL_USER=${MYSQL_USER:-root}

read -s -p "请输入MySQL密码: " MYSQL_PASSWORD
echo
if [ -z "$MYSQL_PASSWORD" ]; then
    echo "错误：密码不能为空"
    exit 1
fi

read -p "请输入MySQL主机地址 (默认: localhost): " MYSQL_HOST
MYSQL_HOST=${MYSQL_HOST:-localhost}

read -p "请输入MySQL端口 (默认: 3306): " MYSQL_PORT
MYSQL_PORT=${MYSQL_PORT:-3306}

echo
echo "正在连接MySQL数据库..."
echo "主机: $MYSQL_HOST"
echo "端口: $MYSQL_PORT"
echo "用户: $MYSQL_USER"
echo

# 测试MySQL连接
echo "正在测试数据库连接..."
if ! mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SELECT 1;" &> /dev/null; then
    echo "错误：无法连接到MySQL数据库，请检查："
    echo "1. MySQL服务是否启动"
    echo "2. 用户名和密码是否正确"
    echo "3. 主机地址和端口是否正确"
    echo "4. 防火墙设置是否允许连接"
    exit 1
fi

echo "数据库连接成功！"
echo

# 创建数据库
echo "正在创建数据库 supplier_management..."
if ! mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS supplier_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" &> /dev/null; then
    echo "错误：创建数据库失败"
    exit 1
fi

echo "数据库创建成功！"
echo

# 运行初始化脚本
echo "正在运行数据库初始化脚本..."
if ! mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" supplier_management < init.sql; then
    echo "错误：运行初始化脚本失败"
    exit 1
fi

echo "数据库初始化完成！"
echo

# 验证数据
echo "正在验证数据..."
mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" supplier_management -e "SELECT 'supplier' as table_name, COUNT(*) as record_count FROM supplier UNION ALL SELECT 'graded_selection_rule' as table_name, COUNT(*) as record_count FROM graded_selection_rule UNION ALL SELECT 'selection_result' as table_name, COUNT(*) as record_count FROM selection_result UNION ALL SELECT 'operation_log' as table_name, COUNT(*) as record_count FROM operation_log;"

echo
echo "========================================"
echo "数据库设置完成！"
echo "========================================"
echo
echo "下一步操作："
echo "1. 修改 src/main/resources/application.properties 中的数据库连接信息"
echo "2. 启动Spring Boot应用程序"
echo "3. 访问 http://localhost:8080 查看系统"
echo
echo "数据库连接信息："
echo "URL: jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/supplier_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true"
echo "用户名: $MYSQL_USER"
echo "密码: $MYSQL_PASSWORD"
echo
