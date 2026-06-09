package com.example.suppliermanagement.service.impl;

import com.example.suppliermanagement.dto.LoginRequest;
import com.example.suppliermanagement.dto.LoginResponse;
import com.example.suppliermanagement.model.User;
import com.example.suppliermanagement.repository.UserRepository;
import com.example.suppliermanagement.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl 单元测试
 * 测试登录、密码验证、Token 管理等核心功能
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthServiceImpl();

        // 通过反射注入依赖
        try {
            var userRepoField = AuthServiceImpl.class.getDeclaredField("userRepository");
            userRepoField.setAccessible(true);
            userRepoField.set(authService, userRepository);

            var encoderField = AuthServiceImpl.class.getDeclaredField("passwordEncoder");
            encoderField.setAccessible(true);
            encoderField.set(authService, passwordEncoder);
        } catch (Exception e) {
            fail("依赖注入失败: " + e.getMessage());
        }

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRealName("测试用户");
        testUser.setRole("USER");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("登录成功：用户名和密码正确应返回 Token")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("测试用户", response.getRealName());
        assertEquals("USER", response.getRole());
        assertFalse(response.getToken().isEmpty());
    }

    @Test
    @DisplayName("登录失败：用户名不存在应抛出异常")
    void testLoginUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(userRepository.findByUsernameAndIsActiveTrue("nonexistent"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录失败：密码错误应抛出异常")
    void testLoginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
                .thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("Token 验证：有效 Token 应返回 true")
    void testValidateToken_Valid() {
        // 先登录获取 Token
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        LoginResponse response = authService.login(request);
        String token = response.getToken();

        // 验证 Token
        boolean isValid = authService.validateToken(token);
        assertTrue(isValid, "有效 Token 应返回 true");
    }

    @Test
    @DisplayName("Token 验证：无效 Token 应返回 false")
    void testValidateToken_Invalid() {
        boolean isValid = authService.validateToken("invalid-token-12345");
        assertFalse(isValid, "无效 Token 应返回 false");
    }

    @Test
    @DisplayName("Token 验证：null Token 应返回 false")
    void testValidateToken_Null() {
        boolean isValid = authService.validateToken(null);
        assertFalse(isValid, "null Token 应返回 false");
    }

    @Test
    @DisplayName("Token 验证：空字符串 Token 应返回 false")
    void testValidateToken_Empty() {
        boolean isValid = authService.validateToken("");
        assertFalse(isValid, "空字符串 Token 应返回 false");
    }

    @Test
    @DisplayName("登出：应移除 Token")
    void testLogout() {
        // 先登录
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        LoginResponse response = authService.login(request);
        String token = response.getToken();

        // 验证 Token 有效
        assertTrue(authService.validateToken(token));

        // 登出
        authService.logout(token);

        // 验证 Token 已失效
        assertFalse(authService.validateToken(token));
    }

    @Test
    @DisplayName("根据 Token 获取用户名")
    void testGetUsernameByToken() {
        // 先登录
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsernameAndIsActiveTrue("testuser"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        LoginResponse response = authService.login(request);
        String token = response.getToken();

        // 获取用户名
        String username = authService.getUsernameByToken(token);
        assertEquals("testuser", username, "应返回正确的用户名");
    }

    @Test
    @DisplayName("根据无效 Token 获取用户名应返回 null")
    void testGetUsernameByToken_Invalid() {
        String username = authService.getUsernameByToken("invalid-token");
        assertNull(username, "无效 Token 应返回 null");
    }

    @Test
    @DisplayName("BCrypt 密码验证：相同密码不同哈希应验证通过")
    void testBCryptPasswordVerification() {
        String password = "password123";
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // BCrypt 每次生成的哈希都不同（因为有随机盐）
        assertNotEquals(hash1, hash2, "两次加密结果应不同");

        // 但验证都应通过
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));
    }

    @Test
    @DisplayName("BCrypt 密码验证：错误密码应验证失败")
    void testBCryptPasswordVerification_WrongPassword() {
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        String hash = passwordEncoder.encode(correctPassword);

        assertFalse(passwordEncoder.matches(wrongPassword, hash),
                "错误密码不应通过验证");
    }
}
