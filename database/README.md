# MySQL数据库设置说明

## 前置要求

1. 安装MySQL 8.0或更高版本
2. 确保MySQL服务正在运行
3. 具有创建数据库和表的权限

## 数据库配置

### 1. 修改application.properties

项目已配置为使用MySQL数据库，默认配置如下：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/supplier_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
```

**请根据您的实际MySQL配置修改以下参数：**
- `localhost:3306` - MySQL服务器地址和端口
- `root` - MySQL用户名
- `123456` - MySQL密码

### 2. 创建数据库

有两种方式创建数据库：

#### 方式1：使用MySQL命令行
```sql
mysql -u root -p
CREATE DATABASE supplier_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 方式2：使用提供的SQL脚本
```bash
mysql -u root -p < init.sql
```

### 3. 运行初始化脚本

执行`database/init.sql`脚本将自动：
- 创建所需的表结构
- 创建必要的索引
- 插入示例数据（10个供应商，3个分级规则）

## 表结构说明

### supplier（供应商表）
- 基本信息：名称、信用代码、资质等级、地区、行业等
- 联系信息：地址、联系人、电话、邮箱
- 业务信息：经营范围、过往业绩、成立时间等
- 企业信息：法人、注册资本、经营状态、企业规模
- 资质信息：资质材料、认证日期、到期日期

### graded_selection_rule（分级选择规则表）
- 规则配置：资质等级、占比、数量限制
- 限制条件：行业、地区限制
- 状态管理：启用/禁用状态

### selection_result（选择结果表）
- 选择信息：类型、数量、条件
- 结果数据：JSON格式的供应商信息
- 操作记录：操作人、重试次数、原因

### operation_log（操作日志表）
- 操作记录：类型、描述、操作人
- 执行信息：请求数据、响应数据、执行时间
- 状态跟踪：成功/失败状态、错误信息

## 示例数据

初始化脚本包含以下示例数据：

### 供应商数据（10条）
- **A级供应商**：北京科技、上海制造、西安软件
- **B级供应商**：广州贸易、深圳电子、武汉物流、重庆制造
- **C级供应商**：杭州网络、成都建筑、南京咨询

### 分级规则（3条）
- A级：占比60%，数量2-3家
- B级：占比30%，数量1-2家
- C级：占比10%，数量0-1家

## 常见问题

### 1. 连接失败
- 检查MySQL服务是否启动
- 验证用户名和密码是否正确
- 确认数据库名称是否正确

### 2. 字符编码问题
- 确保数据库使用utf8mb4字符集
- 检查连接字符串中的编码参数

### 3. 时区问题
- 连接字符串已配置为GMT+8时区
- 如需修改，调整`serverTimezone`参数

### 4. 权限问题
- 确保用户具有创建、修改、删除表的权限
- 检查用户是否有访问数据库的权限

## 验证安装

启动应用后，可以通过以下方式验证数据库连接：

1. 访问 `http://localhost:8080/api/suppliers` 查看供应商列表
2. 检查控制台日志，确认数据库连接成功
3. 使用MySQL客户端查看表结构和数据

## 备份和恢复

### 备份数据库
```bash
mysqldump -u root -p supplier_management > backup.sql
```

### 恢复数据库
```bash
mysql -u root -p supplier_management < backup.sql
```
