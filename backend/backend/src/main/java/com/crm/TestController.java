package com.crm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    // 自动连接数据库
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 测试接口
    @GetMapping("/test")
    public String test() {
        return "✅ CRM 项目启动成功！连接数据库正常！";
    }

    // 读取客户表数据（真正连库查询）
    @GetMapping("/api/customers")
    public List<Map<String, Object>> getCustomers() {
        return jdbcTemplate.queryForList("SELECT * FROM customer");
    }
}