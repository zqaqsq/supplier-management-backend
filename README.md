# 供应商管理系统

## 项目简介

这是一个完整的供应商管理系统，支持供应商信息管理、随机抽取、分级抽取等功能。系统采用前后端分离架构，后端使用Spring Boot + JPA，前端使用HTML + CSS + JavaScript。

## 功能特性

### 1. 供应商信息管理
- **基本信息维护**: 支持供应商名称、统一社会信用代码、资质等级、地区、行业等信息的增删改查
- **高级搜索**: 支持多条件组合查询，可按供应商名称、资质等级、地区、行业、经营范围、过往业绩等进行精确或模糊查询
- **数据导入导出**: 支持Excel格式的数据导入导出，方便批量操作
- **资质材料管理**: 支持资质材料文件路径记录、认证日期、到期日期等管理

### 2. 随机抽取供应商
- **随机抽取**: 从符合资格的供应商库中随机抽取3-5家供应商
- **可视化效果**: 抽取过程通过滚动显示供应商名称等可视化方式呈现，营造公平随机的视觉效果
- **操作日志**: 全程记录操作日志，确保操作可追溯

### 3. 分级抽取供应商
- **等级划分**: 支持对供应商进行等级划分（如A级、B级等）
- **规则配置**: 可自定义各级别供应商的抽取比例、数量上限或下限等规则
- **智能抽取**: 系统依据分级规则和目标抽取数量，从对应级别中结合随机算法抽取供应商
- **结果管理**: 抽取结果生成后，支持查看、打印、导出等操作

### 4. 抽取结果管理
- **结果查看**: 支持查看抽取结果的详细信息
- **重新抽取**: 若对结果不满意，在权限允许范围内可重新抽取
- **原因记录**: 记录重新抽取的原因及次数，确保合规性
- **结果导出**: 支持将抽取结果导出为Excel格式

## 技术架构

### 后端技术栈
- **Spring Boot 2.7.5**: 主框架
- **Spring Data JPA**: 数据持久化
- **H2 Database**: 内存数据库（可替换为MySQL、PostgreSQL等）
- **Apache POI**: Excel导入导出
- **Swagger**: API文档
- **Lombok**: 代码简化

### 前端技术栈
- **HTML5**: 页面结构
- **CSS3**: 样式设计
- **JavaScript ES6+**: 交互逻辑
- **Bootstrap 5**: UI框架
- **Bootstrap Icons**: 图标库

## 快速开始

### 环境要求
- Java 11+
- Maven 3.6+
- 现代浏览器（Chrome、Firefox、Safari、Edge）

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd supplier-management-backend
```

2. **编译项目**
```bash
mvn clean compile
```

3. **运行项目**
```bash
mvn spring-boot:run
```

4. **访问系统**
- 前端页面: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui/
- H2数据库控制台: http://localhost:8080/h2-console

### 数据库配置
默认使用H2内存数据库，如需使用其他数据库，请修改`application.properties`文件：

```properties
# MySQL配置示例
spring.datasource.url=jdbc:mysql://localhost:3306/supplier_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

## API接口说明

### 供应商管理接口
- `GET /api/suppliers` - 分页查询供应商
- `POST /api/suppliers` - 创建供应商
- `PUT /api/suppliers/{id}` - 更新供应商
- `DELETE /api/suppliers/{id}` - 删除供应商
- `POST /api/suppliers/search` - 高级搜索供应商
- `POST /api/suppliers/import` - 导入供应商数据
- `POST /api/suppliers/export` - 导出供应商数据

### 供应商抽取接口
- `POST /api/suppliers/random-select` - 随机抽取供应商
- `POST /api/suppliers/graded-select` - 分级抽取供应商
- `POST /api/suppliers/retry-selection/{resultId}` - 重新抽取供应商

### 分级抽取规则接口
- `GET /api/graded-selection-rules` - 获取所有规则
- `POST /api/graded-selection-rules` - 创建规则
- `PUT /api/graded-selection-rules/{id}` - 更新规则
- `DELETE /api/graded-selection-rules/{id}` - 删除规则
- `POST /api/graded-selection-rules/{id}/toggle-status` - 切换规则状态

## 使用说明

### 1. 供应商管理
1. 在"供应商管理"页面，可以查看所有供应商信息
2. 使用高级搜索功能，按条件筛选供应商
3. 点击"新增供应商"按钮，填写供应商信息并保存
4. 支持Excel批量导入和导出功能

### 2. 随机抽取
1. 在"随机抽取"页面，设置抽取数量（3-5家）
2. 选择资质等级和行业限制（可选）
3. 输入操作人姓名
4. 点击"开始抽取"按钮，系统将随机抽取符合条件的供应商

### 3. 分级抽取
1. 在"抽取规则"页面，配置各级别的抽取规则
2. 设置资质等级、抽取数量、占比等参数
3. 在"分级抽取"页面，输入操作人姓名
4. 点击"开始分级抽取"按钮，系统按规则进行抽取

### 4. 抽取结果管理
1. 查看抽取结果，支持打印和导出
2. 如对结果不满意，可点击"重新抽取"按钮
3. 系统会记录重新抽取的原因和次数

## 数据模型

### 供应商实体 (Supplier)
- 基本信息：名称、统一社会信用代码、资质等级、地区、行业
- 联系信息：联系人、电话、邮箱、地址
- 经营信息：经营范围、过往业绩、成立时间、法人、注册资本
- 状态信息：经营状态、企业规模、资质材料、认证日期、到期日期

### 分级抽取规则 (GradedSelectionRule)
- 规则名称、资质等级、抽取数量、占比
- 行业限制、地区限制、数量上下限
- 规则状态、描述信息

### 抽取结果 (SelectionResult)
- 抽取类型、总数量、抽取条件
- 抽取结果、操作人、重试次数、重试原因
- 创建时间、IP地址

## 部署说明

### 开发环境
```bash
mvn spring-boot:run
```

### 生产环境
```bash
mvn clean package
java -jar target/supplier-management-0.0.1-SNAPSHOT.jar
```

### Docker部署
```bash
# 构建镜像
docker build -t supplier-management .

# 运行容器
docker run -p 8080:8080 supplier-management
```

## 注意事项

1. **数据安全**: 生产环境中请使用强密码和HTTPS
2. **数据库备份**: 定期备份数据库数据
3. **文件上传**: 注意文件大小限制和类型验证
4. **权限控制**: 建议集成Spring Security进行权限管理
5. **日志监控**: 生产环境建议配置日志收集和监控

## 常见问题

### Q: 如何修改数据库连接？
A: 修改`application.properties`文件中的数据库配置，并添加相应的数据库驱动依赖。

### Q: 如何添加新的抽取规则？
A: 在"抽取规则"页面点击"新增规则"，填写规则信息并保存。

### Q: 如何导入大量供应商数据？
A: 使用Excel导入功能，先下载模板，按格式填写数据后上传。

### Q: 抽取结果不满意怎么办？
A: 可以点击"重新抽取"按钮，系统会记录重新抽取的原因。

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目。

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

如有问题或建议，请联系开发团队。
