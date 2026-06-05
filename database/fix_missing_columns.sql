-- ============================================
-- 修复：business 表缺少 update_time 列
-- 在 MySQL 中执行此脚本即可
-- ============================================

USE crm_system;

-- 检查并添加 update_time 列（如果不存在）
ALTER TABLE business ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
AFTER create_time;
