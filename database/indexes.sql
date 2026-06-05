-- ============================================
-- CRM 系统性能优化索引
-- 如果 crm.sql 已执行，直接单独执行此文件即可
-- ============================================

USE crm_system;

-- 业务表：按客户ID查询、按状态筛选
CREATE INDEX idx_business_customer_id ON business(customer_id);
CREATE INDEX idx_business_status ON business(status);

-- 工单表：按客户/业务关联查询、按状态筛选
CREATE INDEX idx_work_order_customer_id ON work_order(customer_id);
CREATE INDEX idx_work_order_business_id ON work_order(business_id);
CREATE INDEX idx_work_order_status ON work_order(status);

-- 客户表：加速名称/电话搜索（前缀索引，节省空间）
CREATE INDEX idx_customer_name ON customer(name(20));

-- 人员表：加速角色筛选
CREATE INDEX idx_staff_role ON staff(role);
