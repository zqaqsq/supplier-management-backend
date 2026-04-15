// 全局变量存储规则和日志
let selectionRules = [];
let currentEditingRuleId = null;
let operationLogs = [];
let operationLogsPageInfo = { totalElements: 0 };

// ==================== 抽取规则管理功能 ====================

// 显示新增规则弹窗
function showAddRuleModal() {
    const modal = new bootstrap.Modal(document.getElementById('addRuleModal'));
    document.getElementById('addRuleForm').reset();
    currentEditingRuleId = null;
    modal.show();
}

// 保存规则
function saveRule() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        showError('请先登录');
        return;
    }
    
    // 获取表单数据
    const ruleData = {
        ruleName: document.getElementById('ruleName').value.trim(),
        qualification: document.getElementById('ruleQualification').value,
        count: parseInt(document.getElementById('ruleCount').value),
        percentage: parseInt(document.getElementById('rulePercentage').value),
        industry: document.getElementById('ruleIndustry').value.trim(),
        region: document.getElementById('ruleRegion').value.trim(),
        description: document.getElementById('ruleDescription').value.trim()
    };
    
    // 验证必填字段
    if (!ruleData.name || !ruleData.qualification || isNaN(ruleData.count) || isNaN(ruleData.percentage)) {
        showError('请填写所有必填字段');
        return;
    }
    
    // 调用保存API
    const url = currentEditingRuleId ? `/api/graded-selection-rules/${currentEditingRuleId}` : '/api/graded-selection-rules';
    const method = currentEditingRuleId ? 'PUT' : 'POST';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(ruleData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showSuccess(currentEditingRuleId ? '规则更新成功' : '规则保存成功');
            // 关闭弹窗
            const ruleModal = bootstrap.Modal.getInstance(document.getElementById('addRuleModal'));
            ruleModal.hide();
            // 重新加载规则列表
            loadSelectionRules();
        } else {
            showError(data.message || '保存失败');
        }
    })
    .catch(error => {
        console.error('保存规则失败:', error);
        showError('保存失败，请检查网络连接');
    });
}

// 加载抽取规则
function loadSelectionRules() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        console.log('未登录，跳过规则加载');
        return;
    }
    
    fetch('/api/graded-selection-rules', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success && data.data) {
            selectionRules = data.data;
            renderRulesTable();
        } else {
            console.error('加载规则失败');
        }
    })
    .catch(error => {
        console.error('加载规则失败:', error);
    });
}

// 渲染规则表格
function renderRulesTable() {
    const rulesTableBody = document.getElementById('rulesTableBody');
    if (!rulesTableBody) return;
    
    rulesTableBody.innerHTML = '';
    
    selectionRules.forEach((rule, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${rule.ruleName}</td>
            <td>${rule.qualification}</td>
            <td>${rule.count}</td>
            <td>${rule.percentage}%</td>
            <td>${rule.industry || '-'}</td>
            <td>${rule.region || '-'}</td>
            <td>${rule.isActive ? '<span class="badge bg-success">启用</span>' : '<span class="badge bg-secondary">停用</span>'}</td>
            <td>
                <button type="button" class="btn btn-primary btn-sm me-1" onclick="editRule(${rule.id})">编辑</button>
                <button type="button" class="btn btn-warning btn-sm me-1" onclick="toggleRule(${rule.id})">${rule.isActive ? '停用' : '启用'}</button>
                <button type="button" class="btn btn-danger btn-sm me-1" onclick="deleteRule(${rule.id})">删除</button>
                <button type="button" class="btn btn-success btn-sm" onclick="applyRule(${rule.id})">应用</button>
            </td>
        `;
        rulesTableBody.appendChild(row);
    });
}

// 编辑规则
function editRule(ruleId) {
    const rule = selectionRules.find(r => r.id === ruleId);
    if (!rule) return;
    
    // 填充表单数据
    document.getElementById('ruleName').value = rule.ruleName;
    document.getElementById('ruleQualification').value = rule.qualification;
    document.getElementById('ruleCount').value = rule.count;
    document.getElementById('rulePercentage').value = rule.percentage;
    document.getElementById('ruleIndustry').value = rule.industry || '';
    document.getElementById('ruleRegion').value = rule.region || '';
    document.getElementById('ruleDescription').value = rule.description || '';
    currentEditingRuleId = rule.id;
    
    // 显示弹窗
    const modal = new bootstrap.Modal(document.getElementById('addRuleModal'));
    modal.show();
}

// 删除规则
function deleteRule(ruleId) {
    if (!confirm('确定要删除这条规则吗？')) return;
    
    const token = localStorage.getItem('authToken');
    if (!token) {
        showError('请先登录');
        return;
    }
    
    fetch(`/api/graded-selection-rules/${ruleId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showSuccess('规则删除成功');
            loadSelectionRules();
        } else {
            showError(data.message || '删除失败');
        }
    })
    .catch(error => {
        console.error('删除规则失败:', error);
        showError('删除失败，请检查网络连接');
    });
}

// 启用/停用规则
function toggleRule(ruleId) {
    const token = localStorage.getItem('authToken');
    if (!token) {
        showError('请先登录');
        return;
    }
    fetch(`/api/graded-selection-rules/${ruleId}/toggle-status`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(r => r.json())
    .then(data => {
        if (data.success) {
            showSuccess(data.message || '状态已更新');
            loadSelectionRules();
        } else {
            showError(data.message || '状态更新失败');
        }
    })
    .catch(err => {
        console.error('切换规则状态失败:', err);
        showError('切换规则状态失败');
    });
}

// 应用规则到抽取功能
function applyRule(ruleId) {
    const rule = selectionRules.find(r => r.id === ruleId);
    if (!rule) return;
    
    // 根据规则类型应用到不同的抽取功能
    const activeSection = document.querySelector('.section:not([style*="display: none"])').id;
    
    if (activeSection === 'random-selection') {
        // 应用到随机抽取
        document.getElementById('randomCount').value = rule.count;
        document.getElementById('randomQualification').value = rule.qualification;
        document.getElementById('randomIndustry').value = rule.industry || '';
        showSuccess(`已应用规则: ${rule.name}`);
    } else if (activeSection === 'graded-selection') {
        // 应用到分级抽取
        showSuccess(`已应用规则: ${rule.name} 到分级抽取`);
    }
    
    // 添加操作日志
    addLogEntry('应用抽取规则', `应用了规则: ${rule.name}`, document.getElementById('currentUser').textContent);
}

// ==================== 操作日志管理功能 ====================

// 加载操作日志
function loadOperationLogs() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        console.log('未登录，跳过日志加载');
        return;
    }
    
    // 获取搜索条件
    const operationType = document.getElementById('logOperationType').value;
    const operator = document.getElementById('logOperator').value.trim();
    const startTime = document.getElementById('logStartTime').value;
    const endTime = document.getElementById('logEndTime').value;
    
    const params = new URLSearchParams();
    if (operationType) params.append('operationType', operationType);
    if (operator) params.append('operator', operator);
    if (startTime) params.append('startDate', startTime);
    if (endTime) params.append('endDate', endTime);
    
    fetch(`/api/operation-logs?${params.toString()}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(response => response.json())
    .then(data => {
        // 后端直接返回Spring Page结构
        if (Array.isArray(data.content)) {
            operationLogs = data.content;
            operationLogsPageInfo.totalElements = typeof data.totalElements === 'number' ? data.totalElements : data.content.length;
            renderLogsTable();
            updateLogRecordInfo();
            return;
        }
        // 兼容包裹在{success, data}中的结构
        if (data && data.success && data.data && Array.isArray(data.data.content)) {
            operationLogs = data.data.content;
            operationLogsPageInfo.totalElements = typeof data.data.totalElements === 'number' ? data.data.totalElements : data.data.content.length;
            renderLogsTable();
            updateLogRecordInfo();
            return;
        }
        console.error('加载日志失败: 返回数据格式不正确');
    })
    .catch(error => {
        console.error('加载日志失败:', error);
    });
}

// 渲染日志表格
function renderLogsTable() {
    const logTableBody = document.getElementById('logTableBody');
    if (!logTableBody) return;
    
    logTableBody.innerHTML = '';
    
    operationLogs.forEach((log, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${index + 1}</td>
            <td>${log.timestamp || log.createdAt || ''}</td>
            <td>${log.operationType}</td>
            <td>${log.content || log.description || ''}</td>
            <td>${log.operator}</td>
            <td>${log.ip || '127.0.0.1'}</td>
            <td><span class="badge bg-success">成功</span></td>
        `;
        logTableBody.appendChild(row);
    });
}

// 刷新操作日志
function refreshOperationLogs() {
    loadOperationLogs();
}

// 导出操作日志
function exportOperationLogs() {
    const token = localStorage.getItem('authToken');
    if (!token) {
        showError('请先登录');
        return;
    }
    
    // 获取搜索条件
    const operationType = document.getElementById('logOperationType').value;
    const operator = document.getElementById('logOperator').value.trim();
    const startTime = document.getElementById('logStartTime').value;
    const endTime = document.getElementById('logEndTime').value;
    
    const params = new URLSearchParams();
    if (operationType) params.append('operationType', operationType);
    if (operator) params.append('operator', operator);
    if (startTime) params.append('startDate', startTime);
    if (endTime) params.append('endDate', endTime);
    
    // 通过fetch携带Authorization下载文件
    const url = `/api/operation-logs/export?${params.toString()}`;
    fetch(url, { headers: { 'Authorization': `Bearer ${token}` } })
        .then(resp => resp.blob())
        .then(blob => {
            const downloadUrl = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = downloadUrl;
            a.download = 'operation-logs.xlsx';
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(downloadUrl);
        })
        .catch(err => {
            console.error('导出失败:', err);
            showError('导出失败');
        });
}

// 重置操作日志搜索
function resetLogSearch() {
    document.getElementById('logSearchForm').reset();
    loadOperationLogs();
}

// 向前端操作日志区域添加一条记录
function addLogEntry(operationType, content, operator) {
    const logTableBody = document.getElementById('logTableBody');
    if (!logTableBody) return;
    
    const now = new Date();
    const timestamp = now.toLocaleString('zh-CN');
    
    const row = document.createElement('tr');
    row.className = 'log-entry-new'; // 用于添加动画效果
    row.innerHTML = `
        <td>${logTableBody.children.length + 1}</td>
        <td>${timestamp}</td>
        <td>${operationType}</td>
        <td>${content}</td>
        <td>${operator}</td>
        <td>${getClientIp()}</td>
        <td><span class="badge bg-success">成功</span></td>
    `;
    
    // 添加到表格顶部
    if (logTableBody.firstChild) {
        logTableBody.insertBefore(row, logTableBody.firstChild);
    } else {
        logTableBody.appendChild(row);
    }
    
    // 添加动画效果
    setTimeout(() => {
        row.classList.remove('log-entry-new');
    }, 10);
    
    // 更新记录信息
    updateLogRecordInfo();
    
    // 将日志添加到全局数组
    operationLogs.unshift({
        timestamp: timestamp,
        operationType: operationType,
        content: content,
        operator: operator,
        ip: getClientIp()
    });
}

// 获取客户端IP地址（简化版）
function getClientIp() {
    return '127.0.0.1'; // 实际环境中可以从服务端获取
}

// 更新日志记录信息
function updateLogRecordInfo() {
    const logTableBody = document.getElementById('logTableBody');
    const recordInfo = document.getElementById('logRecordInfo');
    if (!logTableBody || !recordInfo) return;
    
    const totalOnPage = logTableBody.children.length;
    const total = operationLogsPageInfo.totalElements || totalOnPage;
    
    if (totalOnPage > 0) {
        recordInfo.textContent = `显示第 1-${totalOnPage} 条，共 ${total} 条记录`;
    } else {
        recordInfo.textContent = '暂无记录';
    }
}

// ==================== 抽取功能集成 ====================

// 为抽取功能添加日志记录
function enhanceExtractionFunctions() {
    // 保存原始函数
    const originalStopRandomExtraction = stopRandomExtraction;
    const originalStopGradedExtraction = stopGradedExtraction;
    
    // 增强随机抽取功能
    window.stopRandomExtraction = function() {
        originalStopRandomExtraction();
        
        // 记录抽取结果
        const boxes = document.querySelectorAll('#randomResultContainer .result-box');
        const selectedSuppliers = [];
        
        boxes.forEach(box => {
            selectedSuppliers.push(box.textContent);
        });
        
        const operator = document.getElementById('randomOperator').value || document.getElementById('currentUser').textContent;
        addLogEntry('随机抽取供应商', `抽取了 ${selectedSuppliers.length} 家供应商：${selectedSuppliers.join(', ')}`, operator);
    };
    
    // 增强分级抽取功能
    window.stopGradedExtraction = function() {
        originalStopGradedExtraction();
        
        // 记录抽取结果
        const boxes = document.querySelectorAll('#gradedResultContainer .result-box');
        const selectedSuppliers = [];
        
        boxes.forEach(box => {
            selectedSuppliers.push(box.textContent);
        });
        
        const operator = document.getElementById('gradedOperator').value || document.getElementById('currentUser').textContent;
        addLogEntry('分级抽取供应商', `抽取了 ${selectedSuppliers.length} 家供应商：${selectedSuppliers.join(', ')}`, operator);
    };
}

// 初始化规则和日志功能
function initRulesAndLogs() {
    // 增强抽取功能
    enhanceExtractionFunctions();
    
    // 监听页面切换事件
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function() {
            setTimeout(() => {
                const activeSection = document.querySelector('.section:not([style*="display: none"])').id;
                if (activeSection === 'selection-rules') {
                    loadSelectionRules();
                } else if (activeSection === 'operation-logs') {
                    loadOperationLogs();
                }
            }, 100);
        });
    });
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initRulesAndLogs();
    // 监听日志筛选控件变化，自动刷新
    const opType = document.getElementById('logOperationType');
    const operator = document.getElementById('logOperator');
    const start = document.getElementById('logStartTime');
    const end = document.getElementById('logEndTime');
    [opType, operator, start, end].forEach(el => {
        if (el) {
            el.addEventListener('change', () => loadOperationLogs());
            if (el.id === 'logOperator') {
                el.addEventListener('keyup', () => loadOperationLogs());
            }
        }
    });
});