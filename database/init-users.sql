-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO users (username, password, real_name, email, role, is_active) VALUES 
('admin', 'MD5_HASH_OF_admin123', '系统管理员', 'admin@example.com', 'ADMIN', true)
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试用户账号 (密码: user123)
INSERT INTO users (username, password, real_name, email, role, is_active) VALUES 
('user', 'MD5_HASH_OF_user123', '普通用户', 'user@example.com', 'USER', true)
ON DUPLICATE KEY UPDATE username=username;
