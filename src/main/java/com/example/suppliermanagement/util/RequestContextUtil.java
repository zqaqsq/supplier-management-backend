package com.example.suppliermanagement.util;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * 请求上下文工具类
 * 用于在 Service 层获取当前请求的用户信息和 IP 地址
 */
@Component
public class RequestContextUtil {

    private static final String ATTRIBUTE_USERNAME = "current_username";
    private static final String ATTRIBUTE_USER_ID = "current_user_id";

    /**
     * 设置当前用户名（通常在认证拦截器或 Controller 中调用）
     */
    public static void setCurrentUsername(String username) {
        setAttribute(ATTRIBUTE_USERNAME, username);
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        return (String) getAttribute(ATTRIBUTE_USERNAME);
    }

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        setAttribute(ATTRIBUTE_USER_ID, userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        Object userId = getAttribute(ATTRIBUTE_USER_ID);
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    /**
     * 获取客户端真实 IP 地址
     */
    public static String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取当前 HttpServletRequest
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取当前 HttpSession
     */
    public static HttpSession getCurrentSession() {
        ServletRequestAttributes attributes = getRequestAttributes();
        return attributes != null ? attributes.getRequest().getSession(false) : null;
    }

    private static ServletRequestAttributes getRequestAttributes() {
        try {
            return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        } catch (IllegalStateException e) {
            // 不在 web 请求上下文中
            return null;
        }
    }

    private static void setAttribute(String name, Object value) {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(name, value, ServletRequestAttributes.SCOPE_REQUEST);
        }
    }

    private static Object getAttribute(String name) {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes != null) {
            return attributes.getAttribute(name, ServletRequestAttributes.SCOPE_REQUEST);
        }
        return null;
    }
}
