# MySQL数据库快速启动指南

## 🚀 快速开始

### 1. 安装MySQL
- 下载地址：https://dev.mysql.com/downloads/mysql/
- 选择MySQL 8.0或更高版本
- 安装时记住root密码

### 2. 启动MySQL服务
- Windows: 在服务管理器中启动"MySQL80"服务
- 或使用命令：`net start MySQL80`

### 3. 运行数据库设置脚本

#### Windows用户：
```cmd
# 双击运行
database\setup.bat

# 或使用PowerShell
powershell -ExecutionPolicy Bypass -File database\setup.ps1
```

#### Linux/Mac用户：
```bash
# 给脚本添加执行权限
chmod +x database/setup.sh

# 运行脚本
./database/setup.sh
```

### 4. 修改配置文件
将`database/application-mysql.properties`的内容复制到`src/main/resources/application.properties`

### 5. 启动应用
```bash
mvn spring-boot:run
```

### 6. 访问系统
- 前端界面：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui/

## 📋 手动设置步骤

如果自动脚本无法运行，可以手动执行以下步骤：

### 1. 连接MySQL
```bash
mysql -u root -p
```

### 2. 创建数据库
```sql
CREATE DATABASE supplier_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 运行初始化脚本
```bash
mysql -u root -p supplier_management < database/init.sql
```

### 4. 验证数据
```sql
USE supplier_management;
SHOW TABLES;
SELECT COUNT(*) FROM supplier;
```

## 🔧 常见问题解决

### 问题1：MySQL连接失败
**解决方案：**
- 检查MySQL服务是否启动
- 验证用户名和密码
- 确认端口号（默认3306）

### 问题2：权限不足
**解决方案：**
```sql
GRANT ALL PRIVILEGES ON supplier_management.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### 问题3：字符编码问题
**解决方案：**
- 确保数据库使用utf8mb4字符集
- 检查连接字符串中的编码参数

### 问题4：时区问题
**解决方案：**
- 连接字符串已配置为GMT+8时区
- 如需修改，调整`serverTimezone`参数

## 📊 数据库结构

系统包含以下4个主要表：

1. **supplier** - 供应商信息表
2. **graded_selection_rule** - 分级选择规则表
3. **selection_result** - 选择结果表
4. **operation_log** - 操作日志表

## 📝 示例数据

初始化脚本会自动插入：
- 10个示例供应商（A级3个，B级4个，C级3个）
- 3个分级选择规则
- 完整的表结构和索引

## 🔍 验证安装

启动应用后，可以通过以下方式验证：

1. 访问供应商列表API：`GET /api/suppliers`
2. 检查控制台日志，确认数据库连接成功
3. 使用MySQL客户端查看表结构和数据

## 📞 获取帮助

如果遇到问题：
1. 检查控制台错误日志
2. 确认MySQL版本兼容性
3. 验证网络连接和防火墙设置
4. 参考完整的README.md文档
