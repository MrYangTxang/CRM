package com.crm.config;

import com.crm.entity.Business;
import com.crm.entity.Customer;
import com.crm.entity.Staff;
import com.crm.mapper.BusinessMapper;
import com.crm.mapper.CustomerMapper;
import com.crm.mapper.StaffMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据初始化器：确保系统启动时基础数据存在。
 * - 管理员和测试账号（密码 BCrypt 加密）
 * - 示例客户数据
 * - 示例业务数据
 * 每次启动都会检查，兼容已执行过 SQL 但密码为明文或数据缺失的情况。
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private BusinessMapper businessMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        log.info("========== 数据初始化开始 ==========");
        try {
            ensureStaff("admin", "系统管理员", "admin", "123456");
            ensureStaff("zhangsan", "张三", "employee", "123456");
        } catch (Exception e) {
            log.error("人员数据初始化失败: {}", e.getMessage());
        }
        try {
            ensureCustomers();
        } catch (Exception e) {
            log.error("客户数据初始化失败: {}", e.getMessage());
        }
        try {
            ensureBusinesses();
        } catch (Exception e) {
            log.error("业务数据初始化失败: {}", e.getMessage());
        }
        log.info("========== 数据初始化完成 ==========");
    }

    private void ensureStaff(String username, String name, String role, String rawPassword) {
        Staff staff = staffMapper.findByUsername(username);
        if (staff == null) {
            staff = new Staff();
            staff.setUsername(username);
            staff.setName(name);
            staff.setRole(role);
            staff.setPassword(encoder.encode(rawPassword));
            staffMapper.insert(staff);
            log.info("创建账号: {}", username);
        } else if (!staff.getPassword().startsWith("$2a$")) {
            staff.setPassword(encoder.encode(rawPassword));
            staffMapper.updateById(staff);
            log.info("加密账号密码: {}", username);
        }
    }

    /**
     * 确保示例客户数据存在（仅当客户表为空时插入）
     */
    private void ensureCustomers() {
        List<Customer> existing = customerMapper.selectList(null);
        if (existing != null && !existing.isEmpty()) {
            log.info("客户数据已存在 ({} 条)，跳过初始化", existing.size());
            return;
        }
        log.info("客户表为空，开始插入示例客户数据...");
        Customer c1 = new Customer();
        c1.setName("大连XX科技有限公司");
        c1.setContactPerson("张三");
        c1.setPhone("13800001111");
        c1.setEmail("zhang@xxtech.com");
        c1.setAddress("大连市高新园区");
        customerMapper.insert(c1);

        Customer c2 = new Customer();
        c2.setName("沈阳YY商贸有限公司");
        c2.setContactPerson("李四");
        c2.setPhone("13912345678");
        c2.setEmail("li@yytrade.com");
        c2.setAddress("沈阳市沈河区");
        customerMapper.insert(c2);
        log.info("示例客户数据插入完成 (2 条)");
    }

    /**
     * 确保示例业务数据存在（仅当业务表为空时插入）
     */
    private void ensureBusinesses() {
        List<Business> existing = businessMapper.selectList(null);
        if (existing != null && !existing.isEmpty()) {
            log.info("业务数据已存在 ({} 条)，跳过初始化", existing.size());
            return;
        }
        log.info("业务表为空，开始插入示例业务数据...");
        // 获取已存在的客户（通过名称匹配）
        List<Customer> customers = customerMapper.selectList(null);
        Integer customerId1 = null;
        Integer customerId2 = null;
        if (customers != null) {
            for (Customer c : customers) {
                if (c.getName() != null && c.getName().contains("大连")) {
                    customerId1 = c.getId();
                } else if (c.getName() != null && c.getName().contains("沈阳")) {
                    customerId2 = c.getId();
                }
            }
        }
        // 如果没找到对应客户，使用第一个客户的ID
        if (customerId1 == null && customers != null && !customers.isEmpty()) {
            customerId1 = customers.get(0).getId();
        }
        if (customerId2 == null && customers != null && customers.size() > 1) {
            customerId2 = customers.get(1).getId();
        } else if (customerId2 == null && customerId1 != null) {
            customerId2 = customerId1; // 后备方案
        }

        Business b1 = new Business();
        b1.setName("企业官网开发");
        b1.setType("技术服务");
        b1.setPrice(new BigDecimal("5000.00"));
        b1.setStatus("进行中");
        b1.setCustomerId(customerId1);
        businessMapper.insert(b1);

        Business b2 = new Business();
        b2.setName("年度运维支持");
        b2.setType("运维服务");
        b2.setPrice(new BigDecimal("2000.00"));
        b2.setStatus("进行中");
        b2.setCustomerId(customerId1);
        businessMapper.insert(b2);

        Business b3 = new Business();
        b3.setName("CRM系统定制");
        b3.setType("软件开发");
        b3.setPrice(new BigDecimal("15000.00"));
        b3.setStatus("已完成");
        b3.setCustomerId(customerId2);
        businessMapper.insert(b3);
        log.info("示例业务数据插入完成 (3 条)");
    }
}
