# CRM 客户关系管理系统

## 项目简介
本系统为《数据库与信息管理系统》课程设计作品，实现了客户管理、业务管理、工单管理、人员管理四大核心模块，支持数据的导入导出及工单状态流转。后端采用 Spring Boot + MyBatis，数据库使用 MySQL 8.0。

## 技术栈
- 后端：Spring Boot 2.7.18 + MyBatis + MySQL 8.0
- 前端：Vue 3 + Element Plus（后续添加）
- 构建工具：Maven
- 版本控制：Git

## 项目结构
CRM/
├── backend/ # 后端 Spring Boot 代码（pom.xml, src, application.properties）
├── database/ # 数据库脚本（crm.sql 包含建表和测试数据）
├── frontend/ # 前端 Vue 代码（计划中）
├── log/ # 每日开发日志（Markdown 格式）
└── README.md # 本文件


## 快速部署
### 环境要求
- JDK 17
- MySQL 8.0
- Maven 3.8+
- Git

### 数据库配置
1. 安装 MySQL 8.0，创建数据库 `crm_system`
2. 执行 `database/crm.sql` 中的建表和插入测试数据语句
3. 修改 `backend/src/main/resources/application.properties` 中的数据库密码（当前为 123456，请按需修改）

### 后端启动
bash
cd backend
mvn clean install
mvn spring-boot:run

后端服务将运行在 http://localhost:8080

接口测试（示例）
测试连接：GET http://localhost:8080/test → 返回 ✅ CRM 项目启动成功！连接数据库正常！

查询客户列表：GET http://localhost:8080/api/customers （基于 JdbcTemplate 临时接口）

Git 提交规范
每天至少一次提交，提交信息格式为：YYYY-MM-DD 完成XXX模块，日志文件位于 log/ 目录下。

开发进度（截至 2026-06-01）
搭建 MySQL 数据库，完成四张核心表的创建

创建 Spring Boot 项目，配置 MyBatis 和数据库连接

编写测试接口验证数据库连通性

初始化 Git 仓库，完成首次提交并推送到 GitHub

客户管理模块（增删改查 + 导入导出）

业务管理模块

工单管理模块（创建、回单、退单、区域查询）

人员管理模块