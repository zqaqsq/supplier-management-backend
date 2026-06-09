/**
 * 供应商管理系统 - 公共工具模块
 * 提供 API 调用封装、Token 管理、提示消息等通用功能
 */

// ========== API 配置 ==========
const API_BASE = '';

// ========== Token 管理 ==========

/**
 * 获取认证 Token
 */
function getAuthToken() {
    return localStorage.getItem('authToken');
}

/**
 * 获取用户信息
 */
function getUserInfo() {
    const userInfo = localStorage.getItem('userInfo');
    if (!userInfo) return null;
    try {
        return JSON.parse(userInfo);
    } catch (e) {
        return null;
    }
}

/**
 * 清除登录状态
 */
function clearAuth() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
}

// ========== API 调用封装 ==========

/**
 * 发送带认证的 API 请求
 * @param {string} url - 请求 URL
 * @param {object} options - fetch 选项
 * @returns {Promise<object>} - 返回 JSON 数据
 */
async function apiFetch(url, options = {}) {
    const token = getAuthToken();
    const defaultHeaders = {
        'Content-Type': 'application/json'
    };

    if (token) {
        defaultHeaders['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers
        }
    });

    // 401 时清除登录状态并跳转
    if (response.status === 401) {
        clearAuth();
        window.location.href = 'login.html';
        throw new Error('未登录或登录已过期');
    }

    const data = await response.json();
    return data;
}

/**
 * GET 请求
 */
async function apiGet(url, params = {}) {
    const searchParams = new URLSearchParams(params);
    const queryString = searchParams.toString();
    const fullUrl = queryString ? `${url}?${queryString}` : url;
    return apiFetch(fullUrl, { method: 'GET' });
}

/**
 * POST 请求
 */
async function apiPost(url, body = {}) {
    return apiFetch(url, {
        method: 'POST',
        body: JSON.stringify(body)
    });
}

/**
 * PUT 请求
 */
async function apiPut(url, body = {}) {
    return apiFetch(url, {
        method: 'PUT',
        body: JSON.stringify(body)
    });
}

/**
 * DELETE 请求
 */
async function apiDelete(url) {
    return apiFetch(url, { method: 'DELETE' });
}

// ========== 提示消息 ==========

/**
 * 显示成功提示
 */
function showSuccess(message) {
    showToast(message, 'success');
}

/**
 * 显示错误提示
 */
function showError(message) {
    showToast(message, 'danger');
}

/**
 * 显示 Toast 提示
 */
function showToast(message, type = 'info') {
    // 移除已存在的 toast
    const existingToast = document.querySelector('.app-toast');
    if (existingToast) {
        existingToast.remove();
    }

    const toast = document.createElement('div');
    toast.className = `app-toast toast-notification toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="bi bi-${type === 'success' ? 'check-circle' : type === 'danger' ? 'exclamation-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        </div>
    `;

    // 添加样式
    const style = document.createElement('style');
    style.textContent = `
        .app-toast {
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            padding: 12px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: slideIn 0.3s ease;
            max-width: 400px;
        }
        .app-toast.toast-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .app-toast.toast-danger { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .app-toast.toast-info { background: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }
        .toast-content { display: flex; align-items: center; gap: 8px; }
        .toast-content i { font-size: 18px; }
        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
    `;
    document.head.appendChild(style);
    document.body.appendChild(toast);

    // 3秒后自动移除
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease forwards';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ========== 分页工具 ==========

/**
 * 构建分页查询参数
 */
function buildPageParams(page, size, sortBy = 'createdAt', sortDirection = 'DESC') {
    return {
        page: page - 1, // 后端使用 0-based 索引
        size,
        sortBy,
        sortDirection
    };
}

/**
 * 格式化日期
 */
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN');
}

// ========== 数据转换工具 ==========

/**
 * 将资质等级转换为前端显示格式
 * 后端返回 "A" -> 前端显示 "A级"
 */
function formatQualification(qualification) {
    if (!qualification) return '-';
    if (['A', 'B', 'C', 'D'].includes(qualification)) {
        return qualification + '级';
    }
    return qualification;
}

/**
 * 将资质等级转换为后端存储格式
 * 前端 "A级" -> 后端 "A"
 */
function normalizeQualification(qualification) {
    if (!qualification) return qualification;
    const match = qualification.match(/^([A-D])级?$/);
    return match ? match[1] : qualification;
}

// ========== 导出工具 ==========

/**
 * 下载文件
 */
function downloadFile(blob, filename) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
}
