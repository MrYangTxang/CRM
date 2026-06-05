-- ============================================================
-- CRM 系统 v2 升级迁移脚本 (兼容 MySQL 8.0+)
-- 涵盖：VIP等级、客户标签、跟进记录、工单优先级、工单状态扩展、
--       数据权限隔离、操作日志、导出历史
-- 注意：如果列已存在，对应的 ALTER 语句会报错（可忽略，继续执行后续语句）
-- ============================================================

USE crm_system;

-- 1. 客户表：VIP等级 + 客户标签 + 销售负责人
ALTER TABLE customer ADD COLUMN vip_level VARCHAR(20) DEFAULT '普通会员' COMMENT 'VIP等级';
ALTER TABLE customer ADD COLUMN tags VARCHAR(500) COMMENT '客户标签';
ALTER TABLE customer ADD COLUMN sales_person INT COMMENT '负责人ID';

-- 2. 业务表：负责人
ALTER TABLE business ADD COLUMN owner_id INT COMMENT '负责人ID';

-- 3. 工单表：优先级 + 负责人
ALTER TABLE work_order ADD COLUMN priority VARCHAR(10) DEFAULT '中' COMMENT '优先级';
ALTER TABLE work_order ADD COLUMN owner_id INT COMMENT '负责人ID';

-- 4. 工单状态扩展：ENUM → VARCHAR（支持更多状态值）
ALTER TABLE work_order MODIFY COLUMN status VARCHAR(20) DEFAULT 'created' COMMENT '工单状态: created/processing/completed/returned/cancelled';

-- 5. 客户跟进记录表
CREATE TABLE IF NOT EXISTS follow_up (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '跟进记录ID',
    customer_id INT NOT NULL COMMENT '客户ID',
    content TEXT NOT NULL COMMENT '跟进内容',
    staff_id INT COMMENT '跟进人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) COMMENT='客户跟进记录表';

-- 6. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    username VARCHAR(50) COMMENT '操作用户',
    method VARCHAR(10) COMMENT '请求方法',
    url VARCHAR(200) COMMENT '请求URL',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT '操作IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) COMMENT='操作日志表';

-- 7. 数据导出历史表
CREATE TABLE IF NOT EXISTS export_history (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '导出ID',
    table_name VARCHAR(50) COMMENT '导出表名',
    file_name VARCHAR(200) COMMENT '文件名',
    record_count INT COMMENT '导出记录数',
    username VARCHAR(50) COMMENT '导出用户',
    file_path VARCHAR(300) COMMENT '文件保存路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '导出时间'
) COMMENT='数据导出历史表';
