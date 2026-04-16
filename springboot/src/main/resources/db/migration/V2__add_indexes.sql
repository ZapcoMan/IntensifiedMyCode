-- ==========================================================
-- IntensifiedMyCode 数据库版本管理 V2
-- 添加索引优化查询性能
-- ==========================================================

-- audit_log 索引（原始表有 AUTO_INCREMENT=875，已有数据）
ALTER TABLE `audit_log` ADD INDEX `idx_audit_username` (`username`);
ALTER TABLE `audit_log` ADD INDEX `idx_audit_created_at` (`created_at`);

-- menu 索引（支持按 role 快速筛选菜单）
ALTER TABLE `menu` ADD INDEX `idx_menu_role` (`role`);
