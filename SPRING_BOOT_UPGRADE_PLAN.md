# Spring Boot 2.7.5 → 3.2.x 升级规划

## 当前状态

- Spring Boot 版本：**2.7.5**（2023年11月已停止OSS支持）
- Java 版本：**11**
- Spring Boot 3.x 要求：**Java 17+**

## 升级路线

### 阶段一：平滑升级到 2.7.x 最新补丁版（零破坏性）

**目标版本**：Spring Boot **2.7.18**（2.7.x 最后一个版本，LTS）

**依赖变更**：
```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>  <!-- 当前 2.7.5 -->
</parent>
```

**需要检查的依赖兼容性**：
- springdoc-openapi: `1.6.15` → `1.8.0`（2.7.x 最新）
- spring-security-crypto: 由 starter-parent 统一管理

**验证步骤**：
1. `mvn clean compile` 无报错
2. `mvn test` 全部通过
3. 启动应用，登录、CRUD、抽取功能回归测试

---

### 阶段二：准备 JDK 17 环境

**升级 JDK**：
- 当前：**JDK 11**
- 目标：**JDK 17**（LTS）

```bash
# 使用 jenv 或手动安装
sdk install java 17.0.9-tem
```

**pom.xml 更新**：
```xml
<properties>
    <java.version>17</java.version>
</properties>
```

**验证**：项目使用 JDK 17 编译运行无警告。

---

### 阶段三：Spring Boot 2.7.x → 3.2.x 迁移

**目标版本**：Spring Boot **3.2.5**

#### 3.1 Jakarta EE 迁移（最关键）

Spring Boot 3.x 使用 **Jakarta EE 9+**，包名从 `javax.*` 改为 `jakarta.*`。

需要修改的文件：

```bash
# 一键替换所有 javax.* 为 jakarta.*
find src -name "*.java" -exec sed -i 's/javax\./jakarta./g' {} \;
```

涉及的主要包：

| 原包名 | 新包名 |
|--------|--------|
| `javax.persistence.*` | `jakarta.persistence.*` |
| `javax.servlet.*` | `jakarta.servlet.*` |
| `javax.validation.*` | `jakarta.validation.*` |
| `javax.annotation.*` | `jakarta.annotation.*` |
| `javax.ws.rs.*` | `jakarta.ws.rs.*` |

#### 3.2 依赖版本升级

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>

<properties>
    <java.version>17</java.version>
</properties>

<!-- springdoc 需升级到 2.x -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>2.5.0</version>
</dependency>

<!-- Spring Security Crypto 不再单独引入，由 spring-boot-starter-security 管理 -->
<!-- 如果引入 spring-boot-starter-security，需额外配置 -->
```

#### 3.3 Spring Security 配置变化

如果项目引入 `spring-boot-starter-security`：

```java
// Spring Security 6.x 配置方式变化

// 旧写法 (Spring Security 5.x)
http
    .authorizeRequests()
    .antMatchers("/api/auth/**").permitAll()
    .anyRequest().authenticated();

// 新写法 (Spring Security 6.x)
http
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .anyRequest().authenticated());
```

#### 3.4 数据库连接池

Spring Boot 3.x 内置 HikariCP 8.x，无需额外配置。

---

### 阶段四：回归测试与灰度发布

1. **单元测试**（已在 `src/test/` 中编写）
2. **集成测试**：启动完整应用，测试所有 API
3. **性能测试**：对比升级前后的 QPS 和内存占用
4. **灰度发布**：先在测试环境运行 1 周，再上生产

---

## 风险评估

| 风险项 | 影响 | 缓解措施 |
|--------|------|---------|
| Jakarta EE 迁移 | 高 | 使用 IDE 重构工具批量替换 |
| JDK 版本升级 | 中 | 使用 Docker 容器运行，不污染宿主机 |
| 依赖兼容 | 中 | 升级前先检查 changelog |
| API 行为变化 | 低 | Spring Boot 3.x 对 REST API 影响极小 |

## 预计工时

- 阶段一（2.7.5 → 2.7.18）：**1天**
- 阶段二（JDK 17 环境准备）：**1天**
- 阶段三（迁移到 3.2.x）：**3-5天**
- 阶段四（测试与发布）：**3天**

**总计：约 2 周**

---

## 快速检查清单

升级前后运行以下命令验证：

```bash
# 编译
mvn clean compile

# 测试
mvn test

# 打包
mvn clean package -DskipTests

# 启动（测试环境）
java -jar target/supplier-management-0.0.1-SNAPSHOT.jar
```

**必须通过的回归测试用例**：
1. 登录 /api/auth/login
2. 获取供应商列表 /api/suppliers
3. 搜索供应商 /api/suppliers/search
4. 随机抽取 /api/selection-results/random-select
5. 分级抽取 /api/selection-results/graded-select
