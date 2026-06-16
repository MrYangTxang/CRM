package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.BusinessException;
import com.crm.entity.Business;
import com.crm.entity.Customer;
import com.crm.entity.WorkOrder;
import com.crm.mapper.BusinessMapper;
import com.crm.mapper.CustomerMapper;
import com.crm.mapper.WorkOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 回收站服务：管理 customer / business / work_order 三表的软删除记录
 * 安全机制：表名白名单校验，防 SQL 注入
 */
@Slf4j
@Service
public class RecycleBinService {

    private static final Set<String> ALLOWED_TABLES = Set.of("customer", "business", "work_order");

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private WorkOrderMapper workOrderMapper;

    /** 列出指定表的软删除记录 */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listDeleted(String tableName) {
        checkTableName(tableName);
        List<Map<String, Object>> result = new ArrayList<>();

        switch (tableName) {
            case "customer":
                customerMapper.selectList(
                        new LambdaQueryWrapper<Customer>().eq(Customer::getDeleted, 1)
                ).forEach(c -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("tableName", "customer");
                    row.put("recordId", c.getId());
                    row.put("recordName", c.getName());
                    row.put("deletedTime", c.getUpdateTime());
                    result.add(row);
                });
                break;
            case "business":
                businessMapper.selectList(null).forEach(b -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("tableName", "business");
                    row.put("recordId", b.getId());
                    row.put("recordName", b.getName());
                    row.put("deletedTime", b.getUpdateTime());
                    result.add(row);
                });
                break;
            case "work_order":
                workOrderMapper.selectList(null).forEach(w -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("tableName", "work_order");
                    row.put("recordId", w.getId());
                    row.put("recordName", w.getOrderNo());
                    row.put("deletedTime", w.getUpdateTime());
                    result.add(row);
                });
                break;
        }
        return result;
    }

    /** 各表已删除记录统计 */
    @Transactional(readOnly = true)
    public Map<String, Long> stats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("customer", customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>().eq(Customer::getDeleted, 1)));
        // Business 和 WorkOrder 暂不使用 deleted字段（后续改造）
        stats.put("business", 0L);
        stats.put("work_order", 0L);
        return stats;
    }

    /** 恢复软删除记录 */
    @Transactional
    public void restore(String tableName, Integer recordId) {
        checkTableName(tableName);
        switch (tableName) {
            case "customer":
                Customer customer = customerMapper.selectById(recordId);
                if (customer == null) throw new BusinessException("记录不存在");
                if (customer.getDeleted() == null || customer.getDeleted() == 0) {
                    throw new BusinessException("该记录未被删除");
                }
                customer.setDeleted(0);
                customerMapper.updateById(customer);
                log.info("回收站恢复: table=customer, id={}, name={}", recordId, customer.getName());
                break;
            case "business":
                // Business 暂时使用物理删除，恢复时需重新插入
                throw new BusinessException("业务表暂不支持恢复，数据已物理删除");
            case "work_order":
                throw new BusinessException("工单表暂不支持恢复，数据已物理删除");
        }
    }

    /** 彻底删除（物理删除） */
    @Transactional
    public void permanentDelete(String tableName, Integer recordId) {
        checkTableName(tableName);
        switch (tableName) {
            case "customer":
                Customer customer = customerMapper.selectById(recordId);
                if (customer == null) throw new BusinessException("记录不存在");
                customerMapper.deleteById(recordId);
                log.info("彻底删除: table=customer, id={}", recordId);
                break;
            case "business":
                businessMapper.deleteById(recordId);
                break;
            case "work_order":
                workOrderMapper.deleteById(recordId);
                break;
        }
    }

    private void checkTableName(String tableName) {
        if (tableName == null || !ALLOWED_TABLES.contains(tableName)) {
            throw new BusinessException("非法的表名: " + tableName);
        }
    }
}
