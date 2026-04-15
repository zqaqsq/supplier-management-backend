package com.example.suppliermanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResponse {
    
    private String token;
    private String username;
    private String realName;
    private String role;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
}
