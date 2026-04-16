-- ==========================================================
-- Flyway V2: 添加索引优化查询性能
-- ==========================================================

-- user 表索引
ALTER TABLE `user` ADD INDEX `idx_user_username` (`username`);
ALTER TABLE `user` ADD INDEX `idx_user_status` (`status`);

-- audit_log 索引（已在 V1 建了，但确保存在）
ALTER TABLE `audit_log` ADD INDEX `idx_audit_username` (`username`);
ALTER TABLE `audit_log` ADD INDEX `idx_audit_created_at` (`created_at`);
ALTER TABLE `audit_log` ADD INDEX `idx_audit_action` (`action`);

-- notification 索引
ALTER TABLE `notification` ADD INDEX `idx_notification_user` (`user_id`);
ALTER TABLE `notification` ADD INDEX `idx_notification_status` (`status`);
