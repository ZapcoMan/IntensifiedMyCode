-- ==========================================================
-- IntensifiedMyCode 数据库初始化脚本
-- Flyway V1: 初始化所有表结构和基础数据
-- ==========================================================

-- 1. role 角色表
CREATE TABLE IF NOT EXISTS ole (
    id int(11) NOT NULL AUTO_INCREMENT,
    code varchar(30) NOT NULL,
    
ame varchar(50) NOT NULL,
    description varchar(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY ole_code_unique (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO ole (code, 
ame, description) VALUES
    ('SUPER_ADMIN', '超级管理员', '拥有全部管理权限'),
    ('USER', '普通用户', '仅可访问首页');

-- 2. user 用户表
CREATE TABLE IF NOT EXISTS user (
    id int(11) NOT NULL AUTO_INCREMENT,
    username varchar(50) NOT NULL,
    password varchar(100) NOT NULL,
    
ame varchar(20) NOT NULL,
    vatar varchar(255) DEFAULT NULL,
    email varchar(100) DEFAULT NULL,
    phone varchar(20) DEFAULT NULL,
    status varchar(10) NOT NULL DEFAULT 'ENABLE',
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_username (username),
    KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 密码 123456 的 MD5Hex 值: e10adc3949ba59abbe56e057f20f883e
INSERT INTO user (username, password, 
ame, status) VALUES
    ('admin1', 'e10adc3949ba59abbe56e057f20f883e', '超级管理员1', 'ENABLE');

-- 3. user_role
CREATE TABLE IF NOT EXISTS user_role (
    user_id int(11) NOT NULL,
    ole_id int(11) NOT NULL,
    PRIMARY KEY (user_id, ole_id),
    KEY k_ur_role (ole_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO user_role (user_id, ole_id) VALUES (1, 1);

-- 4. menu 菜单表
CREATE TABLE IF NOT EXISTS menu (
    id int(11) NOT NULL AUTO_INCREMENT,
    
ame varchar(50) NOT NULL,
    path varchar(100) NOT NULL,
    parent_id int(11) NOT NULL DEFAULT 0,
    icon varchar(50) DEFAULT NULL,
    order_num int(11) NOT NULL DEFAULT 0,
    ole varchar(20) NOT NULL,
    component varchar(100) DEFAULT NULL,
    status varchar(10) NOT NULL DEFAULT 'ENABLE',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO menu (
ame, path, parent_id, icon, order_num, ole, component, status) VALUES
    ('首页', '/manager/index', 0, 'House', 1, 'USER', 'Index.vue', 'ENABLE'),
    ('用户管理', '', 0, 'User', 2, 'SUPER_ADMIN', '', 'ENABLE'),
    ('超级管理员信息', '/manager/admin', 2, '', 1, 'SUPER_ADMIN', 'Admin.vue', 'ENABLE'),
    ('用户信息', '/manager/user', 2, '', 2, 'SUPER_ADMIN', 'User.vue', 'ENABLE');

-- 5. audit_log 审计日志表
CREATE TABLE IF NOT EXISTS udit_log (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    username varchar(50) NOT NULL,
    ction varchar(100) NOT NULL,
    esource varchar(100) DEFAULT NULL,
    ip_address varchar(50) DEFAULT NULL,
    details text DEFAULT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_audit_username (username),
    KEY idx_audit_created_at (created_at),
    KEY idx_audit_action (ction)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. notification 通知表
CREATE TABLE IF NOT EXISTS 
otification (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    user_id bigint(20) NOT NULL,
    	ype varchar(20) DEFAULT NULL,
    	itle varchar(100) NOT NULL,
    content text DEFAULT NULL,
    status varchar(20) NOT NULL DEFAULT 'UNREAD',
    ead_at timestamp NULL DEFAULT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notification_user (user_id),
    KEY idx_notification_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;