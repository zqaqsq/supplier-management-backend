# 登录重定向修复

## 问题描述

用户反馈修改高级搜索功能后，重新刷新界面又不弹登录界面了，导致未登录用户无法正常访问登录页面。

## 问题分析

经过代码分析，发现以下问题：

1. **登录状态检查逻辑缺陷**：在 `checkLoginStatus` 函数中，当没有找到登录信息时，函数会跳转但后续代码仍可能执行
2. **页面加载时缺少立即检查**：页面加载完成后没有立即检查localStorage中的登录信息
3. **跳转逻辑不够严格**：某些情况下跳转可能被延迟或阻止
4. **页面可见性检测不够完善**：当用户从其他标签页回来时，登录状态检查不够严格
5. **跳转方式不够可靠**：使用 `window.location.href` 可能在某些情况下被阻止

## 解决方案

### 1. 修复登录状态检查函数

确保在登录信息无效时立即跳转：

```javascript
// 检查登录状态
function checkLoginStatus() {
    console.log('checkLoginStatus 函数被调用');
    
    // 检查是否有有效的token和用户信息
    const token = localStorage.getItem('authToken');
    const userInfo = localStorage.getItem('userInfo');
    
    if (!token || !userInfo) {
        console.log('未找到登录信息，跳转到登录页面');
        // 清除可能存在的无效数据
        localStorage.removeItem('authToken');
        localStorage.removeItem('userInfo');
        // 立即跳转，不等待Promise
        window.location.replace('login.html');
        return Promise.resolve(false);
    }
    
    try {
        const user = JSON.parse(userInfo);
        if (!user.username) {
            console.log('用户信息无效，跳转到登录页面');
            localStorage.removeItem('authToken');
            localStorage.removeItem('userInfo');
            // 立即跳转，不等待Promise
            window.location.replace('login.html');
            return Promise.resolve(false);
        }
        
        // 验证token有效性
        return validateToken(token);
    } catch (e) {
        console.log('用户信息解析失败，清除本地存储并跳转');
        localStorage.removeItem('authToken');
        localStorage.removeItem('userInfo');
        // 立即跳转，不等待Promise
        window.location.replace('login.html');
        return Promise.resolve(false);
    }
}
```

### 2. 增强页面加载时的登录检查

在DOMContentLoaded事件中立即检查登录信息，并添加额外的安全检查：

```javascript
// 页面加载完成后的初始化
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOMContentLoaded 事件触发');
    console.log('开始检查登录状态');
    
    // 立即检查localStorage中的登录信息
    const token = localStorage.getItem('authToken');
    const userInfo = localStorage.getItem('userInfo');
    
    console.log('检查到的token:', token ? '存在' : '不存在');
    console.log('检查到的userInfo:', userInfo ? '存在' : '不存在');
    
    if (!token || !userInfo) {
        console.log('页面加载时未找到登录信息，立即跳转');
        // 清除可能存在的无效数据
        localStorage.removeItem('authToken');
        localStorage.removeItem('userInfo');
        // 强制跳转，使用多种方式确保跳转成功
        try {
            window.location.replace('login.html');
            // 如果replace失败，使用setTimeout确保跳转执行
            setTimeout(() => {
                if (window.location.pathname !== '/login.html') {
                    console.log('replace跳转失败，使用href跳转');
                    window.location.href = 'login.html';
                }
            }, 100);
        } catch (e) {
            console.error('跳转失败，尝试其他方式:', e);
            window.location.href = 'login.html';
        }
        return;
    }
    
    // 检查登录状态
    checkLoginStatus().then(isValid => {
        if (isValid) {
            console.log('登录状态检查成功，开始初始化页面');
            // 初始化页面
            initializePage();
            // 启动心跳检测
            startHeartbeat();
        } else {
            console.log('登录状态检查失败，强制跳转到登录页面');
            // 强制跳转到登录页面
            window.location.replace('login.html');
        }
    }).catch(error => {
        console.error('登录状态检查出错:', error);
        // 出错时跳转到登录页面
        window.location.replace('login.html');
    });
});

// 添加额外的安全检查，在页面加载完成后立即执行
window.addEventListener('load', function() {
    console.log('页面完全加载完成，再次检查登录状态');
    const token = localStorage.getItem('authToken');
    const userInfo = localStorage.getItem('userInfo');
    
    if (!token || !userInfo) {
        console.log('页面完全加载后仍未找到登录信息，强制跳转');
        localStorage.removeItem('authToken');
        localStorage.removeItem('userInfo');
        window.location.replace('login.html');
    }
});
```

### 3. 优化页面可见性变化检测

增强页面可见性变化时的登录状态检查：

```javascript
// 页面可见性变化检测，当用户从其他标签页回来时重新检查登录状态
document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
        console.log('页面变为可见，重新检查登录状态');
        const token = localStorage.getItem('authToken');
        const userInfo = localStorage.getItem('userInfo');
        
        if (!token || !userInfo) {
            console.log('页面可见性变化检测未找到登录信息，跳转到登录页面');
            localStorage.removeItem('authToken');
            localStorage.removeItem('userInfo');
            window.location.replace('login.html');
            return;
        }
        
        // 进一步验证token有效性
        validateToken(token).then(isValid => {
            if (!isValid) {
                console.log('页面可见性变化检测token无效，跳转到登录页面');
                window.location.replace('login.html');
            }
        }).catch(error => {
            console.log('页面可见性变化检测token验证失败，跳转到登录页面');
            window.location.replace('login.html');
        });
    }
});
```

### 4. 完善心跳检测

确保心跳检测在token无效时能正确跳转：

```javascript
// 心跳检测，定期验证token有效性
let heartbeatInterval;
function startHeartbeat() {
    heartbeatInterval = setInterval(() => {
        const token = localStorage.getItem('authToken');
        if (token) {
            validateToken(token).then(isValid => {
                if (!isValid) {
                    // 如果token无效，停止心跳检测
                    stopHeartbeat();
                }
            }).catch(error => {
                console.log('心跳检测失败:', error);
            });
        } else {
            // 如果没有token，停止心跳检测并跳转
            stopHeartbeat();
            console.log('心跳检测未找到token，跳转到登录页面');
            window.location.replace('login.html');
        }
    }, 5 * 60 * 1000); // 每5分钟检测一次
}
```

### 5. 使用更可靠的跳转方式

将所有跳转都改为使用 `window.location.replace()`，并添加备用跳转机制：

```javascript
// 使用replace跳转，如果失败则使用href跳转
try {
    window.location.replace('login.html');
    // 如果replace失败，使用setTimeout确保跳转执行
    setTimeout(() => {
        if (window.location.pathname !== '/login.html') {
            console.log('replace跳转失败，使用href跳转');
            window.location.href = 'login.html';
        }
    }, 100);
} catch (e) {
    console.error('跳转失败，尝试其他方式:', e);
    window.location.href = 'login.html';
}
```

### 6. 创建测试页面

创建了 `test-login.html` 页面来验证登录状态检查是否正常工作：

- 实时显示localStorage内容
- 提供手动检查登录状态功能
- 提供清除登录数据功能
- 提供跳转测试功能

## 修改的文件

### 前端文件
- `src/main/resources/static/index.html` - 修复登录状态检查逻辑、页面加载检查和页面可见性检测
- `src/main/resources/static/test-login.html` - 创建登录状态测试页面

## 主要改进点

1. **立即登录检查**：页面加载时立即检查localStorage中的登录信息
2. **严格的跳转逻辑**：确保在登录信息无效时立即跳转，不等待异步操作
3. **增强的可见性检测**：页面变为可见时进行更严格的登录状态验证
4. **完善的心跳检测**：在token无效时能正确跳转到登录页面
5. **清除无效数据**：在跳转前清除可能存在的无效登录信息
6. **可靠的跳转方式**：使用 `window.location.replace()` 替代 `window.location.href`
7. **备用跳转机制**：如果replace失败，使用setTimeout和href作为备用方案
8. **双重安全检查**：在DOMContentLoaded和load事件中都进行登录检查
9. **测试页面**：提供专门的测试页面来验证登录状态检查功能

## 修复的问题

1. **刷新页面不跳转登录**：页面加载时立即检查登录状态
2. **登录状态检查延迟**：确保无效登录信息时立即跳转
3. **页面可见性检测不严格**：增强页面可见性变化时的登录验证
4. **心跳检测跳转失败**：确保心跳检测能正确处理跳转
5. **跳转方式不可靠**：使用更可靠的跳转方式和备用机制
6. **缺少双重检查**：在多个页面事件中都进行登录状态检查

## 测试建议

1. 测试未登录状态下刷新页面是否能正确跳转到登录页面
2. 测试清除localStorage后刷新页面是否能跳转
3. 测试从其他标签页回来时是否能正确检测登录状态
4. 测试token过期后是否能正确跳转
5. 测试网络错误时是否能正确处理跳转
6. 使用 `test-login.html` 页面验证登录状态检查功能
7. 测试各种跳转方式是否都能正常工作

## 预期效果

- 未登录用户刷新页面时能立即跳转到登录页面
- 登录状态检查更加严格和及时
- 页面可见性变化时能正确验证登录状态
- 心跳检测能正确处理各种异常情况
- 跳转更加可靠，不会出现跳转失败的情况
- 用户体验更加流畅和可靠

## 注意事项

1. **跳转时机**：确保在登录信息无效时立即跳转，不等待异步操作
2. **数据清理**：在跳转前清除无效的登录信息
3. **错误处理**：网络错误时也要能正确处理跳转
4. **性能考虑**：避免重复的登录状态检查
5. **用户体验**：确保跳转过程流畅，不出现卡顿
6. **跳转可靠性**：使用多种跳转方式确保跳转成功
7. **测试验证**：使用测试页面验证功能是否正常工作
