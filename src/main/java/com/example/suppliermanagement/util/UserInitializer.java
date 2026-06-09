package com.example.suppliermanagement.util;

import com.example.suppliermanagement.model.User;
import com.example.suppliermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

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
        // 检查是否已有用户
        if (userRepository.count() > 0) {
            System.out.println("用户数据已存在，跳过初始化");
            return;
        }

        System.out.println("开始初始化用户数据...");

        // 创建管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setEmail("admin@example.com");
        admin.setRole("ADMIN");
        admin.setIsActive(true);
        userRepository.save(admin);

        // 创建普通用户
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRealName("普通用户");
        user.setEmail("user@example.com");
        user.setRole("USER");
        user.setIsActive(true);
        userRepository.save(user);

        System.out.println("用户数据初始化完成！");
        System.out.println("管理员账号: admin / admin123");
        System.out.println("普通用户账号: user / user123");
    }
}
