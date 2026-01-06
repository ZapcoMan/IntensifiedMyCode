/*
 Navicat Premium Data Transfer

 Source Server         : localhostMysql5.7
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : localhost:3307
 Source Schema         : intensifiedmycode

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 01/07/2025 15:40:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id(主键)',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号(唯一)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ADMIN' COMMENT '身份',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username_index`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin1', 'e10adc3949ba59abbe56e057f20f883e', 'ADMIN', '管理员1', 'http://localhost:9991/files/download/1751298690385_1751296265210__5_百乐城_来自小红书网页版.jpg');
INSERT INTO `admin` VALUES (7, 'admin2', 'e10adc3949ba59abbe56e057f20f883e', 'ADMIN', '管理员2', 'http://localhost:9991/files/download/1742375044354_a71653afa6162e44fe6417f3df576d97.jpg');

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `action` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `resource` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `ip_address` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `details` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL,
  `created_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;



-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id(主键)',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号(唯一)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username_index`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'user1', '123456', 'USER', '用户1', 'http://localhost:9991/files/download/1742899004787_微信图片_202503251834051.jpg');
INSERT INTO `user` VALUES (2, 'user2', '123456', 'USER', '用户2', 'http://localhost:9991/files/download/1742899025437_微信图片_202503251834052.jpg');
INSERT INTO `user` VALUES (3, 'user3', '123456', 'USER', '用户3', 'http://localhost:9991/files/download/1742899019835_微信图片_202503251834053.jpg');
INSERT INTO `user` VALUES (5, 'user4', '123456', 'USER', '用户4', 'http://localhost:9991/files/download/1742899014576_微信图片_20250325183349.jpg');
INSERT INTO `user` VALUES (6, 'user5', '123456', 'USER', '用户5', 'http://localhost:9991/files/download/1742375243400_a71653afa6162e44fe6417f3df576d97.jpg');

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单路径',
  `parent_id` int(11) NOT NULL DEFAULT 0 COMMENT '父菜单ID',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `order_num` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色权限',
  `component` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLE' COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of menu
-- ----------------------------
INSERT INTO `menu` VALUES (1, '首页', '/manager/home', 0, 'House', 1, 'USER', 'Home.vue', 'ENABLE');
INSERT INTO `menu` VALUES (2, '用户管理', '', 0, 'User', 2, 'ADMIN', '', 'ENABLE');
INSERT INTO `menu` VALUES (3, '管理员信息', '/manager/admin', 2, '', 1, 'ADMIN', 'Admin.vue', 'ENABLE');
INSERT INTO `menu` VALUES (4, '用户信息', '/manager/user', 2, '', 2, 'ADMIN', 'User.vue', 'ENABLE');

SET FOREIGN_KEY_CHECKS = 1;
