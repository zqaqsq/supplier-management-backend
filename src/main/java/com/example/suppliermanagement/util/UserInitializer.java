package com.example.suppliermanagement.util;

import com.example.suppliermanagement.model.User;
import com.example.suppliermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    private void initializeUsers() {
        List<User> allUsers = userRepository.findAll();

        if (allUsers.isEmpty()) {
            System.out.println("[UserInitializer] 用户表为空，创建默认用户...");
            createDefaultUsers();
            return;
        }

        boolean needUpgrade = false;
        for (User user : allUsers) {
            if (!isBCryptHash(user.getPassword())) {
                needUpgrade = true;
                break;
            }
        }

        if (needUpgrade) {
            System.out.println("[UserInitializer] 检测到旧格式密码，正在升级为 BCrypt...");
            upgradePasswords(allUsers);
            System.out.println("[UserInitializer] 密码升级完成！请使用新密码登录：");
            System.out.println("  admin / admin123");
            System.out.println("  user  / user123");
        } else {
            System.out.println("[UserInitializer] 用户密码已是 BCrypt 格式，跳过初始化");
        }
    }

    private void createDefaultUsers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setEmail("admin@example.com");
        admin.setRole("ADMIN");
        admin.setIsActive(true);
        userRepository.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRealName("普通用户");
        user.setEmail("user@example.com");
        user.setRole("USER");
        user.setIsActive(true);
        userRepository.save(user);

        System.out.println("[UserInitializer] 用户数据初始化完成！");
        System.out.println("  管理员账号: admin / admin123");
        System.out.println("  普通用户账号: user / user123");
    }

    private void upgradePasswords(List<User> users) {
        for (User user : users) {
            String newPassword;
            switch (user.getUsername()) {
                case "admin":
                    newPassword = "admin123";
                    break;
                case "user":
                    newPassword = "user123";
                    break;
                default:
                    newPassword = user.getUsername() + "123";
                    break;
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            System.out.println("[UserInitializer]   - 用户 " + user.getUsername() + " 密码已升级");
        }
    }

    private boolean isBCryptHash(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}
