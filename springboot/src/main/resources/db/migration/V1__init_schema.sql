-- ==========================================================
-- IntensifiedMyCode 数据库版本管理 V1
-- 严格对照原始 Navicat 导出文件 + Mapper XML 实际用表
--
-- Mapper 对应关系：
--   AdminMapper  → users 表（JOIN user_roles + roles）
--   UserMapper   → users 表（JOIN user_roles + roles，筛选 role='STUDENT'）
--   MenuMapper   → menu 表
--   AuditLogMapper → audit_log 表
--
-- 原始 SQL 表名是 user（单数），但 Mapper 全部用 users（复数）
-- → 按 Mapper 实际用表名建表，保持一致性
-- ==========================================================

-- ----------------------------
-- 1. audit_log 审计日志表（原始表结构，保持一致）
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL,
    `action` varchar(100) NOT NULL,
    `resource` varchar(100) DEFAULT NULL,
    `ip_address` varchar(50) DEFAULT NULL,
    `details` text DEFAULT NULL,
    `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- ----------------------------
-- 2. role 角色表（先建，user_roles 依赖它）
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `code` varchar(30) NOT NULL COMMENT '角色编码（如 SUPER_ADMIN / STUDENT）',
    `name` varchar(50) NOT NULL COMMENT '角色名称',
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `role_code_unique`(`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `roles` (`id`, `code`, `name`, `description`) VALUES
    (1, 'SUPER_ADMIN', '超级管理员', '拥有全部管理权限'),
    (2, 'DEPT_ADMIN', '部门管理员', '管理部门内部事务'),
    (3, 'CLUB_LEADER', '社团负责人', '管理社团相关事务'),
    (4, 'USER', '普通用户', '仅可访问首页');

-- ----------------------------
-- 3. users 用户表（Mapper 全用 users 表）
--    字段：id/username/password/name/avatar/email/phone/status/created_at
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '账号(唯一)',
    `password` varchar(100) NOT NULL COMMENT '密码(MD5Hex)',
    `name` varchar(20) NOT NULL COMMENT '真实姓名',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '电话',
    `status` varchar(10) NOT NULL DEFAULT 'ENABLE' COMMENT '状态',
    `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `username_index`(`username`),
    INDEX `idx_users_status`(`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始账户：admin， BCrypt = $10$n7nxVZVJaD79vAXKJDr61OlxQne2c0e3QGBVAaMQ0sWwX2zEX7g4C
INSERT INTO `users` (`id`, `username`, `password`, `name`, `avatar`, `email`, `phone`, `status`, `created_at`) VALUES
    (1, 'admin', '$2a$10$6ToV.KD/2X02hDC3xr3a2uDKpkkm08y9S3kg/Muui9pbflp7BRPqW', '超级管理员1', 'http://127.0.0.1:9991/files/download/1767759751403.jpg', NULL, NULL, 'ENABLE', '2026-01-09 18:53:09');

-- ----------------------------
-- 4. user_roles 用户-角色关联表（依赖 users 和 role）
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
    `user_id` int(11) NOT NULL COMMENT '用户ID',
    `role_id` int(11) NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`),
    INDEX `fk_ur_role`(`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- admin1 的角色是 SUPER_ADMIN（role_id=1）
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);

-- ----------------------------
-- 5. menu 动态菜单表（原始表结构）
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `name` varchar(50) NOT NULL COMMENT '菜单名称',
    `path` varchar(100) NOT NULL COMMENT '菜单路径',
    `parent_id` int(11) NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    `icon` varchar(50) DEFAULT NULL COMMENT '菜单图标',
    `order_num` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
    `role` varchar(20) NOT NULL COMMENT '角色权限',
    `component` varchar(100) DEFAULT NULL COMMENT 'Vue组件路径',
    `status` varchar(10) NOT NULL DEFAULT 'ENABLE' COMMENT '状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `menu` (`id`, `name`, `path`, `parent_id`, `icon`, `order_num`, `role`, `component`, `status`) VALUES
    (1, '首页', '/manager/index', 0, 'House', 1, 'USER', 'Home.vue', 'ENABLE'),
    (2, '用户管理', '', 0, 'User', 2, 'SUPER_ADMIN', '', 'ENABLE'),
    (3, '超级管理员信息', '/manager/admin', 2, '', 1, 'SUPER_ADMIN', 'Admin.vue', 'ENABLE'),
    (4, '用户信息', '/manager/user', 2, '', 2, 'SUPER_ADMIN', 'User.vue', 'ENABLE');
