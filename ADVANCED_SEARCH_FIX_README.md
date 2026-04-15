# 高级搜索功能修复

## 问题描述

用户反馈高级搜索功能无法筛选表中的内容，搜索后没有结果或结果不正确。

## 问题分析

经过代码分析，发现以下问题：

1. **搜索表单字段不完整**：缺少行业和企业规模字段，但搜索函数中引用了这些字段
2. **搜索参数不匹配**：前端搜索参数与后端 `SupplierSearchDTO` 字段不完全匹配
3. **表单结构问题**：有多余的HTML标签，导致布局异常
4. **筛选选项未加载**：部分筛选选项没有正确加载到搜索表单中
5. **搜索逻辑不完整**：搜索成功后没有正确更新分页和显示结果

## 解决方案

### 1. 修复搜索表单结构

添加缺失的搜索字段，修复HTML结构：

```html
<!-- 第一行搜索字段 -->
<div class="row">
    <div class="col-md-3 mb-3">
        <label class="form-label">供应商名称</label>
        <input type="text" class="form-control" id="searchName" placeholder="请输入供应商名称">
    </div>
    <div class="col-md-3 mb-3">
        <label class="form-label">资质等级</label>
        <select class="form-select" id="searchQualification">
            <option value="">全部</option>
        </select>
    </div>
    <div class="col-md-3 mb-3">
        <label class="form-label">地区</label>
        <select class="form-select" id="searchRegion">
            <option value="">全部</option>
        </select>
    </div>
    <div class="col-md-3 mb-3">
        <label class="form-label">经营状态</label>
        <select class="form-select" id="searchStatus">
            <option value="">全部</option>
        </select>
    </div>
</div>

<!-- 第二行搜索字段 -->
<div class="row">
    <div class="col-md-3 mb-3">
        <label class="form-label">行业</label>
        <select class="form-select" id="searchIndustry">
            <option value="">全部</option>
        </select>
    </div>
    <div class="col-md-3 mb-3">
        <label class="form-label">企业规模</label>
        <select class="form-select" id="searchScale">
            <option value="">全部</option>
        </select>
    </div>
    <div class="col-md-6 mb-3">
        <!-- 占位，保持布局 -->
    </div>
</div>
```

### 2. 修复搜索函数逻辑

优化搜索函数，确保参数正确传递：

```javascript
// 搜索供应商
function searchSuppliers() {
    // 获取搜索条件
    const name = document.getElementById('searchName').value.trim();
    const qualification = document.getElementById('searchQualification').value;
    const region = document.getElementById('searchRegion').value;
    const status = document.getElementById('searchStatus').value;
    
    console.log('搜索条件:', { name, qualification, region, status });
    
    // 构建搜索参数，只包含有值的字段
    const searchParams = {
        page: 0,
        size: pageSize
    };
    
    if (name) searchParams.name = name;
    if (qualification) searchParams.qualification = qualification;
    if (region) searchParams.region = region;
    if (status) searchParams.status = status;
    
    console.log('发送搜索参数:', searchParams);
    
    // 重置到第一页
    currentPage = 1;
    
    // 执行搜索
    authenticatedFetch('/api/suppliers/search', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(searchParams)
    })
    .then(response => response.json())
    .then(data => {
        console.log('搜索响应:', data);
        if (data.success && data.data) {
            suppliers = data.data.content || [];
            totalPages = data.data.totalPages || 1;
            currentPage = 1; // 重置当前页
            renderSuppliersTable();
            renderPagination();
            showSuccess(`搜索完成，找到 ${suppliers.length} 条记录`);
        } else {
            showError('搜索失败: ' + (data.message || '未知错误'));
            document.getElementById('suppliersTableBody').innerHTML = '<tr><td colspan="9" class="text-center">搜索失败</td></tr>';
        }
    })
    .catch(error => {
        console.error('搜索失败:', error);
        if (error.message !== 'Token无效' && error.message !== '未登录') {
            showError('搜索失败: ' + error.message);
            document.getElementById('suppliersTableBody').innerHTML = '<tr><td colspan="9" class="text-center">网络错误</td></tr>';
        }
    });
}
```

### 3. 完善筛选选项加载

确保所有筛选选项都能正确加载：

```javascript
// 加载筛选选项
function loadFilterOptions() {
    console.log('开始加载筛选选项');
    
    // 检查是否已登录
    const token = localStorage.getItem('authToken');
    if (!token) {
        console.log('未登录，跳过筛选选项加载');
        return;
    }
    
    // 加载资质等级选项
    authenticatedFetch('/api/suppliers/qualifications')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                fillSelectOptions('searchQualification', data.data);
            }
        })
        .catch(error => console.error('加载资质等级选项失败:', error));
    
    // 加载地区选项
    authenticatedFetch('/api/suppliers/regions')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                fillSelectOptions('searchRegion', data.data);
            }
        })
        .catch(error => console.error('加载地区选项失败:', error));
    
    // 加载行业选项
    authenticatedFetch('/api/suppliers/industries')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                fillSelectOptions('searchIndustry', data.data);
            }
        })
        .catch(error => console.error('加载行业选项失败:', error));
    
    // 加载经营状态选项
    authenticatedFetch('/api/suppliers/statuses')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                fillSelectOptions('searchStatus', data.data);
            }
        })
        .catch(error => console.error('加载经营状态选项失败:', error));
    
    // 加载企业规模选项
    authenticatedFetch('/api/suppliers/scales')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                fillSelectOptions('searchScale', data.data);
            }
        })
        .catch(error => console.error('加载企业规模选项失败:', error));
}

// 通用函数：填充选择框选项
function fillSelectOptions(selectId, options) {
    const select = document.getElementById(selectId);
    if (select) {
        select.innerHTML = '<option value="">全部</option>';
        options.forEach(option => {
            const optionElement = document.createElement('option');
            optionElement.value = option;
            optionElement.textContent = option;
            select.appendChild(optionElement);
        });
    }
}
```

### 4. 优化重置搜索功能

确保重置功能能正确清空所有字段：

```javascript
// 重置搜索
function resetSearch() {
    document.getElementById('searchForm').reset();
    currentPage = 1;
    
    // 清空所有搜索字段
    document.getElementById('searchName').value = '';
    document.getElementById('searchQualification').value = '';
    document.getElementById('searchRegion').value = '';
    document.getElementById('searchStatus').value = '';
    document.getElementById('searchIndustry').value = '';
    document.getElementById('searchScale').value = '';
    
    console.log('搜索已重置，重新加载所有数据');
    loadSuppliers();
}
```

### 5. 添加调试日志

在关键位置添加console.log，便于调试：

```javascript
console.log('搜索条件:', { name, qualification, region, status });
console.log('发送搜索参数:', searchParams);
console.log('搜索响应:', data);
console.log('搜索已重置，重新加载所有数据');
```

## 修改的文件

### 前端文件
- `src/main/resources/static/index.html` - 修复搜索表单结构、搜索函数逻辑和筛选选项加载

## 主要改进点

1. **完整的搜索字段**：添加了行业和企业规模字段
2. **正确的参数映射**：确保前端参数与后端DTO字段匹配
3. **动态筛选选项**：从后端API动态加载所有筛选选项
4. **优化的搜索逻辑**：搜索成功后正确更新表格和分页
5. **完善的重置功能**：确保重置后能正确清空所有字段
6. **调试信息**：添加详细的日志输出，便于问题排查

## 测试建议

1. 测试各个搜索字段的筛选功能
2. 测试筛选选项是否正确加载
3. 测试搜索后的结果显示
4. 测试分页功能是否正常
5. 测试重置功能是否清空所有字段
6. 测试组合搜索条件的效果

## 预期效果

- 高级搜索功能能正确筛选供应商数据
- 所有筛选选项都能正确加载和显示
- 搜索结果显示正确，分页功能正常
- 重置功能能完全清空搜索条件
- 用户体验更加流畅和可靠

## 注意事项

1. **字段匹配**：确保前端搜索字段与后端DTO字段完全匹配
2. **API调用**：所有筛选选项加载都需要正确的认证token
3. **错误处理**：搜索失败时要有友好的错误提示
4. **性能优化**：避免重复加载筛选选项
5. **用户体验**：搜索完成后要有成功提示和结果统计





