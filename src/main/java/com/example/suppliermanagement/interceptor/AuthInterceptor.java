package com.example.suppliermanagement.interceptor;

import com.example.suppliermanagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
    
        // 对于登录和验证请求，直接放行
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/api/auth/login") || requestURI.equals("/api/auth/validate")) {
            return true;
        }
    
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"未提供有效的认证token\"}");
            return false;
        }
    
        // 提取token
        String token = authHeader.substring(7);
    
        // 验证token
        if (!authService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"token无效或已过期\"}");
            return false;
        }
    
        // 刷新token(如果需要)，直接调用接口方法
        try {
            String refreshedToken = authService.refreshToken(token);
            if (refreshedToken != null && !refreshedToken.equals(token)) {
                // 设置新token到响应头
                response.setHeader("Authorization", "Bearer " + refreshedToken);
            }
        } catch (Exception e) {
            // 刷新token失败，不影响当前请求
            System.err.println("刷新token失败: " + e.getMessage());
        }
    
        return true;
    }
}
