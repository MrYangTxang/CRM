-- ============================================================
-- CRM 系统 - 核心表建表 + 测试数据（每表 8 条）
-- 可直接执行：source database/seed_data.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS crm_system;
USE crm_system;

-- 如果表已存在则删除重建（仅开发环境使用）
DROP TABLE IF EXISTS work_order;
DROP TABLE IF EXISTS business;
DROP TABLE IF EXISTS follow_up;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS staff;

-- ========================================
-- 1. 客户表
-- ========================================
CREATE TABLE customer (
    id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    name        VARCHAR(100) NOT NULL        COMMENT '客户名称',
    contact_person VARCHAR(50)               COMMENT '联系人',
    phone       VARCHAR(20)                  COMMENT '电话',
    email       VARCHAR(100)                 COMMENT '邮箱',
    address     VARCHAR(200)                 COMMENT '地址',
    vip_level   VARCHAR(20)  DEFAULT '普通会员' COMMENT 'VIP等级',
    tags        VARCHAR(500)                 COMMENT '客户标签',
    sales_person INT                          COMMENT '负责人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='客户信息表';

-- 2. 员工表
CREATE TABLE staff (
    id            INT PRIMARY KEY AUTO_INCREMENT COMMENT '人员ID',
    name          VARCHAR(50)  NOT NULL        COMMENT '姓名',
    username      VARCHAR(50)  UNIQUE NOT NULL COMMENT '登录账号',
    password      VARCHAR(100) NOT NULL        COMMENT '密码（BCrypt 加密）',
    role          VARCHAR(20)  DEFAULT 'employee' COMMENT '角色: admin/employee',
    phone         VARCHAR(20)                   COMMENT '联系电话',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_login_time DATETIME                    COMMENT '最后登录时间',
    last_login_ip VARCHAR(50)                   COMMENT '最后登录IP'
) COMMENT='人员信息表';

-- 3. 业务表
CREATE TABLE business (
    id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '业务ID',
    name        VARCHAR(100) NOT NULL         COMMENT '业务名称',
    type        VARCHAR(50)                   COMMENT '业务类型',
    price       DECIMAL(10,2)                 COMMENT '业务金额',
    status      VARCHAR(20) DEFAULT '进行中'   COMMENT '业务状态',
    customer_id INT                           COMMENT '关联客户ID',
    owner_id    INT                           COMMENT '负责人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL
) COMMENT='业务信息表';

-- 4. 工单表
CREATE TABLE work_order (
    id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    order_no    VARCHAR(50) UNIQUE NOT NULL   COMMENT '工单编号',
    customer_id INT NOT NULL                  COMMENT '客户ID',
    business_id INT NOT NULL                  COMMENT '业务ID',
    region      VARCHAR(50)                   COMMENT '区域',
    status      VARCHAR(20) DEFAULT 'created' COMMENT '工单状态: created/processing/completed/returned/cancelled',
    priority    VARCHAR(10) DEFAULT '中'       COMMENT '优先级: 高/中/低',
    owner_id    INT                           COMMENT '负责人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (customer_id) REFERENCES customer(id),
    FOREIGN KEY (business_id) REFERENCES business(id)
) COMMENT='工单表';


-- ========================================
-- 测试数据
-- ========================================

-- === 客户表 (20 条) ===
INSERT INTO customer (id, name, contact_person, phone, email, address, vip_level, tags, sales_person) VALUES
(1,  '大连海创科技有限公司',   '张伟',   '13804110001', 'zhangwei@haichuang.com',   '大连市高新园区火炬路32号',    'VIP1',   '技术合作,长期客户',        1),
(2,  '沈阳东软商贸有限公司',   '李娜',   '13940220002', 'lina@neusoft-trade.com',  '沈阳市浑南新区新隆街10号',   'VIP2',   '软件采购,年度框架',        1),
(3,  '北京华信达科技有限公司',  '王磊',   '13501130003', 'wanglei@huaxinda.cn',     '北京市海淀区中关村软件园',    'VIP1',   '云计算,战略合作伙伴',      2),
(4,  '上海浦东创新科技有限公司', '赵敏',  '13601840004', 'zhaomin@pdigroup.cn',     '上海市浦东新区张江高科技园区', 'VIP3',   'AI方案,投资级客户',        2),
(5,  '深圳迅捷通信息技术有限公司','陈晓明','13723450005', 'chenxm@xunjietong.com',  '深圳市南山区科技园南区',      '普通会员',   'IT外包,新客户',            3),
(6,  '广州云帆大数据有限公司',   '刘洋',   '13825160006', 'liuyang@yunfan-data.com', '广州市天河区珠江新城',       'VIP2',   '数据服务,月付客户',        3),
(7,  '成都锦程信息技术有限公司', '周强',   '13980770007', 'zhouqiang@jincheng.cn',  '成都市武侯区天府软件园',      '普通会员',   '软件定制,首次合作',        4),
(8,  '杭州网易达科技有限公司',   '吴芳',   '13758240008', 'wufang@wangyida.com',     '杭州市西湖区文三路478号',     'VIP1',   '电商平台,长期客户',        4),
(9,  '北京中关村云数据有限公司', '孙伟',   '13901010009', 'sunwei@zgcloud.cn',       '北京市海淀区中关村大街1号',    'VIP3',   '大数据,战略合作',          2),
(10, '北京朝阳智慧城市科技公司', '周芳',   '13801020010', 'zhoufang@smartcity.cn',   '北京市朝阳区望京科技园',      'VIP2',   '智慧城市,年度客户',        2),
(11, '深圳福田芯联科技公司',    '郑强',   '13603010011', 'zhengqiang@xinlian.com',  '深圳市福田区华强北电子市场',   'VIP2',   '芯片设计,长期合作',        3),
(12, '深圳南山腾讯滨海合作方',  '冯丽',   '13703020012', 'fengli@tencent-partner.cn', '深圳市南山区后海大道88号',   'VIP3',   '互联网,重点客户',          3),
(13, '上海静安数据服务中心',    '陈洁',   '13502010013', 'chenjie@jadata.cn',       '上海市静安区南京西路恒隆广场',  'VIP2',   '数据服务,外资客户',        2),
(14, '成都高新云计算有限公司',  '何涛',   '13608010014', 'hetao@cdcloud.com',       '成都市高新区天府大道中段',     'VIP1',   '云计算,新客户',            4),
(15, '大连金州软件外包中心',    '马超',   '13404110015', 'machao@dloutsource.cn',   '大连市金州区软件产业园区',     'VIP1',   '外包服务,月付客户',        1),
(16, '杭州余杭电商数据公司',    '黄敏',   '13588010016', 'huangmin@hzecom.cn',      '杭州市余杭区未来科技城',      'VIP3',   '电商,投资级客户',          4),
(17, '南京鼓楼信息科技有限公司', '林浩',   '13770010017', 'linhao@njinfo.cn',        '南京市鼓楼区新模范马路66号',   'VIP1',   '信息技术,首次合作',        1),
(18, '武汉光谷人工智能研究院',  '赵雪',   '13971010018', 'zhaoxue@whai.com',        '武汉市洪山区光谷科技园',      'VIP2',   'AI研发,战略合作',          3),
(19, '西安高新区软件开发有限公司','韩冰',  '13892010019', 'hanbing@xasoft.cn',       '西安市高新区软件新城',        'VIP1',   '软件开发,新客户',          4),
(20, '天津滨海大数据服务公司',  '许倩',   '13622010020', 'xuqian@tjbigdata.com',    '天津市滨海新区于家堡金融区',   'VIP2',   '大数据,金融客户',          2);

-- === 员工表 (8 条) ===
-- 密码均为 "123456"，启动后由 DataInitializer 自动 BCrypt 加密
INSERT INTO staff (id, name, username, password, role, phone) VALUES
(1,  '系统管理员',   'admin',      '123456', 'admin',    '13800000001'),
(2,  '张伟',        'zhangwei',   '123456', 'employee', '13804110001'),
(3,  '李娜',        'lina',       '123456', 'employee', '13940220002'),
(4,  '王磊',        'wanglei',    '123456', 'employee', '13501130003'),
(5,  '赵敏',        'zhaomin',    '123456', 'employee', '13601840004'),
(6,  '陈晓明',      'chenxm',     '123456', 'employee', '13723450005'),
(7,  '刘洋',        'liuyang',    '123456', 'employee', '13825160006'),
(8,  '周强',        'zhouqiang',  '123456', 'employee', '13980770007');

-- === 业务表 (8 条) ===
INSERT INTO business (id, name, type, price, status, customer_id, owner_id) VALUES
(1,  '企业官网开发',        '技术服务',  50000.00,  '进行中',   1, 1),
(2,  '年度运维支持',        '运维服务',  20000.00,  '已完成',   1, 2),
(3,  'CRM系统定制开发',     '软件开发', 150000.00,  '进行中',   2, 3),
(4,  '云服务器迁移方案',    '技术服务',  80000.00,  '已完成',   3, 4),
(5,  'AI智能客服系统',      '软件开发', 300000.00,  '进行中',   4, 5),
(6,  '网络安全等级保护测评', '咨询服务',  60000.00,  '已完成',   5, 6),
(7,  '大数据分析平台搭建',  '数据服务', 120000.00,  '进行中',   6, 7),
(8,  '移动APP开发',         '软件开发',  90000.00,  '洽谈中',   7, 8);

-- === 工单表 (8 条) ===
INSERT INTO work_order (id, order_no, customer_id, business_id, region, status, priority, owner_id) VALUES
(1,  'WO202606010001', 1, 1, '大连高新园区',     'created',     '高', 1),
(2,  'WO202606010002', 1, 2, '大连高新园区',     'completed',   '中', 2),
(3,  'WO202606020001', 2, 3, '沈阳浑南新区',     'processing',  '高', 3),
(4,  'WO202606030001', 3, 4, '北京海淀区',       'completed',   '中', 4),
(5,  'WO202606040001', 4, 5, '上海浦东新区',     'created',     '高', 5),
(6,  'WO202606050001', 5, 6, '深圳南山区',       'returned',    '低', 6),
(7,  'WO202606060001', 6, 7, '广州天河区',       'processing',  '中', 7),
(8,  'WO202606070001', 7, 8, '成都高新区',       'cancelled',   '低', 8);

-- === 跟进记录表 (17 条) ===
INSERT INTO follow_up (id, customer_id, content, staff_id, create_time) VALUES
(1,  1,  '与张伟总沟通了官网改版需求，客户对当前方案基本满意，希望在7月中旬前完成上线。已安排技术团队下周一出详细排期。', 1, '2026-06-05 09:30:00'),
(2,  3,  '王磊反馈CRM系统定制需要增加BI报表模块，已收集具体需求文档，预计评估工作量15人天。', 2, '2026-06-05 10:15:00'),
(3,  5,  '陈晓明提出需要增加服务器监控告警功能，已演示现有方案，客户倾向采用开源方案降低成本。', 3, '2026-06-05 11:00:00'),
(4,  9,  '与孙伟总初步接触，介绍了公司大数据解决方案，客户表示很感兴趣，约下周深入交流。', 2, '2026-06-05 14:30:00'),
(5,  12, '冯丽反馈腾讯滨海合作项目二期需求有变更，需要增加移动端适配，已记录并转交产品部门评估。', 3, '2026-06-05 15:45:00'),
(6,  16, '黄敏总确认了电商数据平台的验收时间，定在6月20日，需要提前准备验收材料和培训手册。', 4, '2026-06-05 16:20:00'),
(7,  2,  '李娜反馈年度运维合同续签事宜，报价已提交，等待对方采购部门审批。', 1, '2026-06-04 09:00:00'),
(8,  4,  '赵敏总对AI方案二期规划提出新想法，希望加入大模型能力，约定本周五深入讨论技术路线。', 2, '2026-06-04 10:30:00'),
(9,  11, '郑强反馈芯片设计项目进度正常，已完成前端原型，正在联调后端接口。', 3, '2026-06-04 14:00:00'),
(10, 18, '与赵雪院长讨论了AI研究院的合作框架，双方在技术方向上高度一致，拟签署战略合作协议。', 3, '2026-06-04 16:00:00'),
(11, 6,  '刘洋提出需要增加数据清洗的自动化流程，已给出初步方案和报价，预计下周签约。', 3, '2026-06-03 09:30:00'),
(12, 7,  '周强公司首次合作进展顺利，软件定制一期已交付测试，客户反馈良好，正在洽谈二期需求。', 4, '2026-06-03 11:00:00'),
(13, 13, '陈洁经理对数据服务方案提出了一些定制化需求，已记录并安排技术评估，预计3个工作日内回复。', 2, '2026-06-03 14:30:00'),
(14, 15, '马超反馈软件外包项目测试中发现2个Bug，已协调开发团队修复，预计明天完成。', 1, '2026-06-02 10:00:00'),
(15, 17, '首次拜访林浩总，介绍了公司信息技术解决方案，客户对ERP系统集成表现出兴趣。', 1, '2026-06-02 15:00:00'),
(16, 20, '许倩经理沟通了大数据平台搭建的初步方案，客户对Hadoop生态方案认可度较高。', 2, '2026-06-01 10:30:00'),
(17, 8,  '杭州网易达科技有限公司年度续约谈判完成，合同金额较去年增长20%，预计下周签正式合同。', 4, '2026-06-01 14:00:00');
