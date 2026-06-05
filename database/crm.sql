CREATE DATABASE IF NOT EXISTS crm_system;
USE crm_system;

-- 1. 客户表
CREATE TABLE IF NOT EXISTS customer (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(200) COMMENT '地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='客户信息表';

-- 2. 业务表
CREATE TABLE IF NOT EXISTS business (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '业务ID',
    name VARCHAR(100) NOT NULL COMMENT '业务名称',
    type VARCHAR(50) COMMENT '业务类型',
    price DECIMAL(10,2) COMMENT '业务金额',
    status VARCHAR(20) DEFAULT '进行中' COMMENT '业务状态',
    customer_id INT COMMENT '关联客户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL
) COMMENT='业务信息表';

-- 3. 工单表（关联客户和业务）
CREATE TABLE IF NOT EXISTS work_order (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    order_no VARCHAR(50) UNIQUE NOT NULL COMMENT '工单编号',
    customer_id INT NOT NULL COMMENT '客户ID',
    business_id INT NOT NULL COMMENT '业务ID',
    region VARCHAR(50) COMMENT '区域',
    status ENUM('created','returned','cancelled') DEFAULT 'created' COMMENT '工单状态: created-已创建, returned-已回单, cancelled-已退单',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (customer_id) REFERENCES customer(id),
    FOREIGN KEY (business_id) REFERENCES business(id)
) COMMENT='工单表';

-- 4. 人员表（用户管理）
CREATE TABLE IF NOT EXISTS staff (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '人员ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT '密码（明文或加密）',
    role VARCHAR(20) DEFAULT 'employee' COMMENT '角色: admin/employee',
    phone VARCHAR(20) COMMENT '联系电话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP'
) COMMENT='人员信息表';

-- 插入测试数据
INSERT INTO customer (name, contact_person, phone, email, address) VALUES 
('大连XX科技有限公司', '张三', '13800001111', 'zhang@xxtech.com', '大连市高新园区'),
('沈阳YY商贸有限公司', '李四', '13912345678', 'li@yytrade.com', '沈阳市沈河区');

INSERT INTO business (name, type, price, customer_id) VALUES 
('企业官网开发', '技术服务', 5000.00, 1),
('年度运维支持', '运维服务', 2000.00, 1),
('CRM系统定制', '软件开发', 15000.00, 2);

-- 管理员和测试账号（密码由 DataInitializer 在首次启动时自动 BCrypt 加密）
-- 此处插入的明文密码会在应用启动后被自动加密为 BCrypt 格式
INSERT INTO staff (name, username, password, role) VALUES
('系统管理员', 'admin', '123456', 'admin'),
('张三', 'zhangsan', '123456', 'employee');

-- 可选：插入一条演示工单
INSERT INTO work_order (order_no, customer_id, business_id, region, status) VALUES 
('WO202506010001', 1, 1, '大连高新园区', 'created');