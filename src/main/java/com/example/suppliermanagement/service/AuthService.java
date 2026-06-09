package com.example.suppliermanagement.service;

import com.example.suppliermanagement.dto.LoginRequest;
import com.example.suppliermanagement.dto.LoginResponse;

public interface AuthService {
    
    LoginResponse login(LoginRequest loginRequest);
    
    boolean validateToken(String token);
    
    void logout(String token);
    
    String getCurrentUsername();
    
    // 添加refreshToken方法声明
    String refreshToken(String token);

    /**
     * 根据 token 获取用户名
     */
    String getUsernameByToken(String token);
}
