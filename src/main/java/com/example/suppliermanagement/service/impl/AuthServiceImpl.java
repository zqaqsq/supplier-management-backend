package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.dto.LoginRequest;
import com.example.suppliermanagement.dto.LoginResponse;
import com.example.suppliermanagement.model.User;
import com.example.suppliermanagement.repository.UserRepository;
import com.example.suppliermanagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 存储活跃的token，使用内存存储，页面刷新后失效
    private static final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    
    // Token有效期：8小时，减少频繁登录
    private static final int TOKEN_EXPIRE_MINUTES = 480;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 查找用户
        User user = userRepository.findByUsernameAndIsActiveTrue(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码（使用BCrypt加密，带随机盐值）
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成token
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(TOKEN_EXPIRE_MINUTES);

        // 存储token信息
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUsername(user.getUsername());
        tokenInfo.setUserId(user.getId());
        tokenInfo.setCreateTime(now);
        tokenInfo.setExpireTime(expireTime);
        tokenStore.put(token, tokenInfo);

        // 更新最后登录时间
        user.setLastLogin(now);
        userRepository.save(user);

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setLoginTime(now);
        response.setExpireTime(expireTime);

        return response;
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            return false;
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(tokenInfo.getExpireTime())) {
            tokenStore.remove(token);
            return false;
        }

        return true;
    }

    @Override
    public void logout(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }

    @Override
    public String getCurrentUsername() {
        // 为了避免返回null导致的问题，暂时返回一个默认用户名
        // 实际项目中应该集成Spring Security并从SecurityContext中获取
        return "default_user";
    }

    // 定时清理过期的token，每30分钟执行一次
    @Scheduled(fixedRate = 1800000)
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int beforeSize = tokenStore.size();
        tokenStore.entrySet().removeIf(entry -> 
            entry.getValue().getExpireTime().isBefore(now)
        );
        int afterSize = tokenStore.size();
        if (beforeSize > afterSize) {
            System.out.println("清理过期token: " + (beforeSize - afterSize) + "个");
        }
    }

    // 获取token信息（用于调试）
    public TokenInfo getTokenInfo(String token) {
        return tokenStore.get(token);
    }

    // 获取当前活跃token数量（用于监控）
    public int getActiveTokenCount() {
        return tokenStore.size();
    }

    // Token信息内部类
    private static class TokenInfo {
        private String username;
        private Long userId;
        private LocalDateTime createTime;
        private LocalDateTime expireTime;

        // Getters and Setters
        @SuppressWarnings("unused")
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        @SuppressWarnings("unused")
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        @SuppressWarnings("unused")
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        
        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    }
    // 添加token刷新方法
    // 在类中添加@Override注解来实现接口方法
    @Override
    public String refreshToken(String oldToken) {
        TokenInfo tokenInfo = tokenStore.get(oldToken);
        if (tokenInfo == null) {
            throw new RuntimeException("无效的token");
        }
    
        // 如果token即将过期(30分钟内)，则刷新
        LocalDateTime now = LocalDateTime.now();
        if (now.plusMinutes(30).isAfter(tokenInfo.getExpireTime())) {
            // 移除旧token
            tokenStore.remove(oldToken);
    
            // 生成新token
            String newToken = UUID.randomUUID().toString();
            LocalDateTime expireTime = now.plusMinutes(TOKEN_EXPIRE_MINUTES);
    
            // 更新token信息
            tokenInfo.setCreateTime(now);
            tokenInfo.setExpireTime(expireTime);
            tokenStore.put(newToken, tokenInfo);
    
            return newToken;
        }
    
        return oldToken;
    }
}
