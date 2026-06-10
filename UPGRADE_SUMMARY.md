# 供应商管理系统 - 项目升级总结

> 升级日期：2026-06-10

---

## 一、升级背景

本项目之前使用的是 **MD5 加密** + **手动异常处理** 的旧实现，存在以下隐患：

1. **密码安全性不足**：使用了 `DigestUtils.md5DigestAsHex()` 进行密码加密，MD5 为已被破解的弱哈希算法，无法抵御彩虹表攻击。
2. **业务逻辑错误**：`SupplierServiceImpl` 将供应商资质等级保存为 `"A"`，但 `SelectionServiceImpl` 的权重表使用 `"A级"` 做 key，导致加权算法完全失效，所有供应商命中默认权重 `0.5`。
3. **异常处理不一致**：Controller 层手动 `try-catch` 返回 HTTP 200，导致前端无法通过 HTTP 状态码正确识别失败。
4. **日志信息缺失**：操作日志操作员写死 `"system"`，IP 写死 `"127.0.0.1"`，无法审计真实操作人。
5. **前端代码分散**：所有 JS 逻辑写在 HTML 的 `<script>` 中，无模块划分。
6. **部署依赖本地环境**：需要手动安装 JDK / Maven / MySQL。

---

## 二、升级内容总览

本次升级涉及 **19 个文件修改 + 13 个文件新增**，具体如下：

| 模块 | 说明 | 文件数 |
|------|------|--------|
| 安全 | MD5 → BCrypt 密码加密 | 3 个（`AuthServiceImpl.java` / `UserInitializer.java` / `SecurityConfig.java`） |
| 业务修复 | 资质等级映射统一（`A级` → `A`） | 1 个（`SelectionServiceImpl.java`） |
| 异常处理 | 统一 HTTP 状态码语义 + 移除 Controller try-catch | 3 个（`GlobalExceptionHandler.java` / `AuthController.java` / `SelectionController.java`） |
| 架构优化 | DTO 层引入 + 分页排序白名单校验 | 3 个（`SupplierDTO.java` / `SupplierListDTO.java` / `SupplierServiceImpl.java`） |
| 操作日志 | 记录真实用户名和 IP | 2 个（`RequestContextUtil.java` / `AuthInterceptor.java`） |
| 前端 | 抽取 `app.js` 模块 | 2 个（`app.js` / `index.html`） |
| 缓存 | Caffeine 本地缓存资质等级/地区/状态 | 2 个（`CacheConfig.java` / `SupplierServiceImpl.java`） |
| 部署 | Dockerfile + docker-compose | 3 个（`Dockerfile` / `docker-compose.yml` / `application-docker.properties`） |
| 测试 | 单元测试 26 个 | 4 个（`SelectionServiceImplTest.java` / `AuthServiceImplTest.java` / `SupplierServiceImplTest.java` / `pom.xml`） |
| 文档 | Spring Boot 升级路线图 | 1 个（`SPRING_BOOT_UPGRADE_PLAN.md`） |

---

## 三、详细升级内容

### 3.1 密码加密升级：MD5 → BCrypt

**涉及文件**：
- `src/main/java/com/example/suppliermanagement/config/SecurityConfig.java`（新增）
- `src/main/java/com/example/suppliermanagement/service/impl/AuthServiceImpl.java`
- `src/main/java/com/example/suppliermanagement/util/UserInitializer.java`
- `pom.xml`

**改动说明**：
- 引入 `spring-security-crypto` 依赖，声明 `BCryptPasswordEncoder` Bean。
- `AuthServiceImpl` 中 `DigestUtils.md5DigestAsHex()` → `passwordEncoder.matches()`。
- `UserInitializer` 中初始用户密码由 `passwordEncoder.encode()` 生成。
- **智能升级逻辑**：启动时检测所有用户密码是否为 BCrypt 格式（以 `$2a$`/`$2b$`/`$2y$` 开头），不是则自动重新生成。

**对旧数据的影响**：
- 数据库中已有的 MD5 密码哈希会在应用启动时自动检测并升级为 BCrypt。
- admin 用户密码重置为 `admin123`，user 用户密码重置为 `user123`。

### 3.2 业务修复：资质等级映射统一

**涉及文件**：
- `src/main/java/com/example/suppliermanagement/service/impl/SelectionServiceImpl.java`

**问题**：`SupplierServiceImpl` 存数据时把 `"A级"` 转成 `"A"`，但 `SelectionServiceImpl` 权重表 key 用 `"A级"`，导致所有供应商命中默认权重 `0.5`，加权算法完全失效。

**修复**：3 处映射表 `"A级"/"B级"/"C级"/"D级"` → `"A"/"B"/"C"/"D"`。

**效果**：A 级供应商权重恢复为 `1.0`，B 级 `0.8`，C 级 `0.6`，D 级 `0.4`，智能抽取功能恢复正常。

### 3.3 异常处理统一

**涉及文件**：
- `src/main/java/com/example/suppliermanagement/exception/GlobalExceptionHandler.java`
- `src/main/java/com/example/suppliermanagement/controller/AuthController.java`
- `src/main/java/com/example/suppliermanagement/controller/SelectionController.java`

**改动说明**：
- 移除 Controller 层所有手动 `try-catch`。
- 统一使用 `@ControllerAdvice` 全局异常处理。
- HTTP 状态码语义化：
  - `EntityNotFoundException` → **404 Not Found**
  - `IllegalArgumentException` → **400 Bad Request**
  - `RuntimeException` → **400 Bad Request**
  - `Exception` → **500 Internal Server Error**（隐藏具体错误信息）

### 3.4 DTO 层引入

**涉及文件**：
- `src/main/java/com/example/suppliermanagement/dto/SupplierDTO.java`（新增）
- `src/main/java/com/example/suppliermanagement/dto/SupplierListDTO.java`（新增）
- `src/main/java/com/example/suppliermanagement/service/impl/SupplierServiceImpl.java`

**改动说明**：
- 新增完整供应商 DTO 和列表专用简化 DTO。
- Service 层新增 `convertToDTO()` 和 `convertToListDTO()` 转换方法。
- 通过 `@JsonProperty` 兼容前端字段别名（`contact`/`phone`/`description`/`notes`）。

### 3.5 操作日志真实化

**涉及文件**：
- `src/main/java/com/example/suppliermanagement/util/RequestContextUtil.java`（新增）
- `src/main/java/com/example/suppliermanagement/interceptor/AuthInterceptor.java`
- `src/main/java/com/example/suppliermanagement/service/impl/SupplierServiceImpl.java`

**改动说明**：
- 新增 `RequestContextUtil`：通过 `RequestContextHolder` 获取当前请求的用户名和 IP。
- `AuthInterceptor`：Token 验证通过后设置用户名到 Context。
- `SupplierServiceImpl`：`logOperation()` 改为获取真实用户名和 IP。

### 3.6 分页排序字段白名单

**涉及文件**：
- `src/main/java/com/example/suppliermanagement/service/impl/SupplierServiceImpl.java`

**改动说明**：
- 定义 `ALLOWED_SORT_FIELDS` 白名单集合。
- `getSuppliers()` 和 `searchSuppliers()` 方法校验 `sortBy` 参数，非法字段抛出 `IllegalArgumentException`（400）。

### 3.7 前端模块化

**涉及文件**：
- `src/main/resources/static/js/app.js`（新增）
- `src/main/resources/static/index.html`

**改动说明**：
- 新增 `app.js`，约 240 行，包含：
  - `apiFetch` / `apiGet` / `apiPost` / `apiPut` / `apiDelete` 统一 API 封装
  - `showSuccess` / `showError` / `showToast` 提示工具
  - `getAuthToken` / `getUserInfo` Token 管理
  - `formatQualification` / `normalizeQualification` 资质等级转换
  - `downloadFile` 文件下载工具
- `index.html` 添加 `<script src="js/app.js">` 引用。

### 3.8 缓存策略

**涉及文件**：
- `src/main/java/com/example/suppliermanagement/config/CacheConfig.java`（新增）
- `src/main/java/com/example/suppliermanagement/service/impl/SupplierServiceImpl.java`
- `pom.xml`

**改动说明**：
- 引入 `spring-boot-starter-cache` + `caffeine` 依赖。
- 定义 4 个缓存区域，Caffeine 配置：最大 1000 条，30 分钟过期，异步统计。
- `getAllQualifications`/`getAllRegions`/`getAllStatuses` 加 `@Cacheable`。
- 增删改操作加 `@CacheEvict` 自动失效。

### 3.9 Docker 化部署

**涉及文件**：
- `Dockerfile`（新增）
- `docker-compose.yml`（新增）
- `.dockerignore`（新增）
- `src/main/resources/application-docker.properties`（新增）

**改动说明**：
- **两阶段构建**：Maven 构建层 + JRE 运行层，减小最终镜像体积。
- **健康检查**：通过 `/actuator/health` 监控应用状态。
- **环境变量驱动**：数据库连接信息通过环境变量配置。
- **本地 MySQL 连接**：使用 `host.docker.internal` 连接宿主机 MySQL。

---

## 四、单元测试

**新增测试文件**：
- `src/test/java/com/example/suppliermanagement/service/impl/SelectionServiceImplTest.java`（资质等级权重计算、评分计算、映射一致性）
- `src/test/java/com/example/suppliermanagement/service/impl/AuthServiceImplTest.java`（登录成功/失败、Token 验证/失效/登出、BCrypt 验证）
- `src/test/java/com/example/suppliermanagement/service/impl/SupplierServiceImplTest.java`（资质等级映射、重复信用代码校验、DTO 转换）

**合计 26 个测试用例**。

---

## 五、部署指南

### 方式一：Docker Compose（推荐）

```bash
# 1. 进入项目目录
cd supplier-management-backend

# 2. 确保本地 MySQL 已启动，数据库 supplier_management 已存在
#    mysql -uroot -p1234 -e "CREATE DATABASE IF NOT EXISTS supplier_management DEFAULT CHARACTER SET utf8mb4;"

# 3. 启动
docker-compose up -d --build

# 4. 查看日志
docker logs -f supplier-management-app

# 5. 访问
#    http://localhost:8080
#    账号: admin / admin123
```

### 方式二：本地运行（不使用 Docker）

```bash
# 1. 确保 JDK 11+ 和 Maven 已安装
java -version
mvn -version

# 2. 构建
mvn clean package -DskipTests

# 3. 运行
java -jar target/supplier-management-1.0.0.jar

# 4. 访问 http://localhost:8080
```

---

## 六、数据库密码升级说明

本次升级后，应用首次启动时会自动检测并升级数据库中的用户密码。日志示例：

```
[UserInitializer] 检测到旧格式密码，正在升级为 BCrypt...
[UserInitializer]   - 用户 admin 密码已升级
[UserInitializer]   - 用户 user 密码已升级
[UserInitializer] 密码升级完成！请使用新密码登录：
  admin / admin123
  user  / user123
```

如果你的数据库中还有其他用户（`admin`/`user` 之外的用户名），会自动重置密码为 `用户名 + 123`（例如用户名 `zhangsan`，新密码 `zhangsan123`）。

---

## 七、升级后验证清单

- [ ] **登录功能**：`admin / admin123` 可正常登录
- [ ] **密码加密**：数据库 `users.password` 字段值以 `$2a$`/`$2b$`/`$2y$` 开头
- [ ] **供应商列表**：分页查询正常，非法排序字段返回 400
- [ ] **智能抽取**：A 级供应商权重 > B 级 > C 级 > D 级
- [ ] **操作日志**：新建/修改/删除供应商后，日志中记录真实用户名和 IP
- [ ] **缓存**：资质等级/地区/状态下拉列表查询响应迅速
- [ ] **异常响应**：登录错误返回 HTTP 400 而非 200
- [ ] **Docker 健康检查**：`docker ps` 显示 `healthy` 状态

---

## 八、后续可选优化方向

1. **Redis 分布式缓存**：替换 Caffeine 为 Redis，支持多实例部署时的 Token 和缓存共享。
2. **API 版本控制**：引入 `/api/v1/` 路径前缀，为后续迭代预留空间。
3. **前端渐进式迁移**：将 `index.html` 中剩余业务逻辑逐步迁移到模块化 JS。
4. **Spring Boot 升级**：从 2.7.x 升级到 3.2.x（参考 `SPRING_BOOT_UPGRADE_PLAN.md`）。
5. **操作日志审计增强**：记录变更前后数据快照，支持回溯。
