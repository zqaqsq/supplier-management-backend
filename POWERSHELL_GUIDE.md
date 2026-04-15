# PowerShell 运行指南

## 问题描述
在 PowerShell 中运行 `mvn spring-boot:run` 命令时遇到问题，因为 PowerShell 不支持 `&&` 操作符。

## 解决方案

### 方案1：使用分号分隔命令（推荐）
```powershell
mvn clean compile; mvn spring-boot:run
```

### 方案2：使用 & 操作符
```powershell
mvn clean compile & mvn spring-boot:run
```

### 方案3：分别执行命令
```powershell
# 第一步：清理和编译
mvn clean compile

# 第二步：运行应用
mvn spring-boot:run
```

### 方案4：使用批处理文件
创建 `run.bat` 文件：
```batch
@echo off
mvn clean compile
mvn spring-boot:run
pause
```

然后在 PowerShell 中运行：
```powershell
.\run.bat
```

### 方案5：使用 Git Bash 或 WSL
如果安装了 Git Bash 或 WSL，可以使用：
```bash
# Git Bash
mvn clean compile && mvn spring-boot:run

# WSL
mvn clean compile && mvn spring-boot:run
```

## 推荐步骤

### 1. 使用 PowerShell 分号分隔
```powershell
cd D:\traexiangmu\supplier-management-backend
mvn clean compile; mvn spring-boot:run
```

### 2. 等待应用启动
应用启动后，你应该看到类似输出：
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.x.x)

2024-xx-xx xx:xx:xx.xxx  INFO 1234 --- [           main] c.e.s.SupplierManagementApplication      : Starting SupplierManagementApplication using Java 11.0.x
```

### 3. 访问应用
应用启动成功后，在浏览器中访问：
- 主页：`http://localhost:8080/index.html`
- 登录页：`http://localhost:8080/login.html`
- 调试页：`http://localhost:8080/debug-login.html`
- 简单测试：`http://localhost:8080/test-simple.html`

## 常见问题

### 问题1：端口被占用
如果 8080 端口被占用，可以修改 `application.properties`：
```properties
server.port=8081
```

### 问题2：Java 版本不匹配
确保使用 Java 11 或更高版本：
```powershell
java -version
```

### 问题3：Maven 未安装
确保 Maven 已安装并添加到 PATH：
```powershell
mvn -version
```

## 调试步骤

### 1. 检查后端服务状态
```powershell
# 检查端口是否被占用
netstat -an | findstr :8080

# 或者使用 PowerShell 命令
Get-NetTCPConnection -LocalPort 8080
```

### 2. 检查日志输出
在应用运行时，观察控制台输出，查找错误信息。

### 3. 测试 API 端点
使用浏览器或 Postman 测试：
```
GET http://localhost:8080/api/suppliers/all
POST http://localhost:8080/api/auth/login
```

## 完整启动流程

```powershell
# 1. 进入项目目录
cd D:\traexiangmu\supplier-management-backend

# 2. 清理并编译项目
mvn clean compile

# 3. 启动应用
mvn spring-boot:run

# 4. 等待启动完成（看到 "Started SupplierManagementApplication" 消息）

# 5. 在浏览器中访问 http://localhost:8080/index.html
```

## 注意事项

1. **PowerShell 语法**：PowerShell 使用分号 `;` 而不是 `&&` 来分隔命令
2. **路径问题**：确保在正确的项目目录中运行命令
3. **权限问题**：如果遇到权限问题，尝试以管理员身份运行 PowerShell
4. **防火墙**：确保防火墙允许 8080 端口的访问
5. **依赖下载**：首次运行可能需要下载依赖，请耐心等待

## 故障排除

如果仍然遇到问题，请：
1. 检查控制台错误信息
2. 确认 Java 和 Maven 版本
3. 尝试清理 Maven 缓存：`mvn clean`
4. 检查项目配置文件
5. 提供具体的错误信息以便进一步诊断
