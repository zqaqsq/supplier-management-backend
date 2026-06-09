package com.example.suppliermanagement.controller;

import com.example.suppliermanagement.dto.ApiResponse;
import com.example.suppliermanagement.dto.LoginRequest;
import com.example.suppliermanagement.dto.LoginResponse;
import com.example.suppliermanagement.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        // 添加token到响应头，帮助前端自动保存
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + response.getToken());
        // 设置Access-Control-Expose-Headers，确保前端能读取到Authorization头
        headers.set("Access-Control-Expose-Headers", "Authorization");
        return new ResponseEntity<>(ApiResponse.success("登录成功", response), headers, HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    @GetMapping("/validate")
    @Operation(summary = "验证token")
    public ResponseEntity<ApiResponse<Object>> validateToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            boolean isValid = authService.validateToken(token);
            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success("token有效", true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("token已过期或无效"));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("未提供有效的token"));
    }
}
