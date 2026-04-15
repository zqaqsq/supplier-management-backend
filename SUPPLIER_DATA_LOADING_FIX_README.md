# 供应商数据加载问题修复

## 问题描述

在修复登录跳转问题后，发现无法加载供应商数据，系统显示加载失败或网络错误。

## 问题分析

经过代码分析，发现问题出现在我们新添加的认证拦截器上：

1. **认证拦截器拦截所有API请求**：新创建的 `AuthInterceptor` 会拦截所有 `/api/**` 路径的请求
2. **前端API调用缺少认证头**：大部分API调用没有在请求头中添加 `Authorization: Bearer {token}`
3. **401未授权错误**：缺少认证头的请求被拦截器拦截，返回401状态码
4. **前端错误处理不当**：没有正确处理401错误，导致显示"网络错误"

## 解决方案

### 1. 创建通用认证函数

在 `index.html` 中添加了 `authenticatedFetch` 函数，统一处理认证：

```javascript
// 通用的认证fetch函数
function authenticatedFetch(url, options = {}) {
    const token = localStorage.getItem('authToken');
    if (!token) {
        handleUnauthorized();
        return Promise.reject(new Error('未登录'));
    }
    
    const headers = {
        ...options.headers,
        'Authorization': `Bearer ${token}`
    };
    
    return fetch(url, {
        ...options,
        headers
    }).then(response => {
        if (response.status === 401) {
            handleUnauthorized();
            return Promise.reject(new Error('Token无效'));
        }
        return response;
    });
}
```

### 2. 修复所有需要认证的API调用

将以下函数中的 `fetch` 调用替换为 `authenticatedFetch`：

- `loadSuppliers()` - 加载供应商数据
- `searchSuppliers()` - 搜索供应商
- `saveSupplier()` - 新增供应商
- `updateSupplier()` - 更新供应商
- `deleteSupplier()` - 删除供应商
- `exportSuppliers()` - 导出供应商数据
- `loadFilterOptions()` - 加载筛选选项
  - 资质等级选项
  - 地区选项
  - 行业选项
  - 经营状态选项
  - 企业规模选项

### 3. 修复extraction-modal.html

在 `extraction-modal.html` 中手动添加认证头：

```javascript
function loadSuppliers() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        loadDefaultSuppliers();
        return;
    }
    
    fetch('/api/suppliers/all', {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    // ... 错误处理逻辑
}
```

### 4. 统一错误处理

添加了 `handleUnauthorized()` 函数，统一处理401错误：

```javascript
// 处理未授权错误
function handleUnauthorized() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
    window.location.href = 'login.html';
}
```

## 修改的文件

### 前端文件
- `src/main/resources/static/index.html` - 修复所有API调用，添加认证头
- `src/main/resources/static/extraction-modal.html` - 修复API调用，添加认证头

### 后端文件
- `src/main/java/com/example/suppliermanagement/config/WebConfig.java` - 配置认证拦截器
- `src/main/java/com/example/suppliermanagement/interceptor/AuthInterceptor.java` - 认证拦截器实现

## 主要改进点

1. **统一认证处理**：所有API请求都通过 `authenticatedFetch` 函数处理
2. **自动token管理**：自动添加认证头，自动处理401错误
3. **优雅降级**：认证失败时自动跳转登录页面
4. **错误分类处理**：区分网络错误和认证错误，避免误判

## 测试建议

1. 测试登录后供应商数据正常加载
2. 测试页面刷新后数据加载正常
3. 测试token过期后的处理
4. 测试各种供应商操作（增删改查）
5. 测试筛选选项加载
6. 测试导出功能

## 预期效果

- 登录后能正常加载供应商数据
- 所有供应商相关功能正常工作
- 认证失败时自动跳转登录页面
- 网络错误和认证错误区分处理
- 用户体验更加流畅

## 注意事项

1. **拦截器配置**：确保登录和验证接口被排除在拦截器之外
2. **Token有效期**：当前设置为8小时，可根据需要调整
3. **错误处理**：所有API调用都应该使用 `authenticatedFetch` 函数
4. **降级处理**：某些功能在认证失败时应该有合适的降级方案
