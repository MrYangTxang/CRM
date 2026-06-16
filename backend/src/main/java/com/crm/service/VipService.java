package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.entity.Business;
import com.crm.entity.Customer;
import com.crm.mapper.BusinessMapper;
import com.crm.mapper.CustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * VIP 自动升降级服务
 * 规则（功能清单）：
 * - 普通会员 → VIP1：≥3 笔业务 或 累计≥5万
 * - VIP1 → VIP2：≥5 笔业务 或 累计≥15万
 * - VIP2 → VIP3：≥10 笔业务 或 累计≥50万
 * - 只升级不降级
 */
@Slf4j
@Service
public class VipService {

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 根据客户的业务数据自动计算并更新VIP等级（只升不降）
     */
    @Transactional
    public void autoUpgrade(Integer customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) return;

        // 统计该客户的业务笔数和累计金额
        List<Business> businesses = businessMapper.selectList(
                new LambdaQueryWrapper<Business>().eq(Business::getCustomerId, customerId));
        int count = businesses.size();
        BigDecimal total = businesses.stream()
                .map(b -> b.getPrice() != null ? b.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算应达等级
        String targetLevel = calculateLevel(count, total);
        String currentLevel = customer.getVipLevel();
        if (currentLevel == null || currentLevel.isEmpty()) {
            currentLevel = "普通会员";
        }

        // 只升级不降级
        if (compareVipLevel(targetLevel, currentLevel) > 0) {
            customer.setVipLevel(targetLevel);
            customerMapper.updateById(customer);
            log.info("VIP自动升级: customerId={}, name={}, {} → {} (业务{}笔, 累计{}元)",
                    customerId, customer.getName(), currentLevel, targetLevel, count, total);
        }
    }

    /** 批量重新计算所有客户的VIP等级 */
    @Transactional
    public void recalculateAll() {
        List<Customer> customers = customerMapper.selectList(
                new LambdaQueryWrapper<Customer>().eq(Customer::getDeleted, 0));
        for (Customer c : customers) {
            autoUpgrade(c.getId());
        }
        log.info("全量VIP重新计算完成，共{}个客户", customers.size());
    }

    private String calculateLevel(int count, BigDecimal total) {
        if (count >= 10 || total.compareTo(new BigDecimal("500000")) >= 0) {
            return "VIP3";
        }
        if (count >= 5 || total.compareTo(new BigDecimal("150000")) >= 0) {
            return "VIP2";
        }
        if (count >= 3 || total.compareTo(new BigDecimal("50000")) >= 0) {
            return "VIP1";
        }
        return "普通会员";
    }

    /** 比较VIP等级：VIP3 > VIP2 > VIP1 > 普通会员，返回正值表示level1更高 */
    private int compareVipLevel(String level1, String level2) {
        return vipOrder(level1) - vipOrder(level2);
    }

    private int vipOrder(String level) {
        if ("VIP3".equals(level)) return 4;
        if ("VIP2".equals(level)) return 3;
        if ("VIP1".equals(level)) return 2;
        if ("普通会员".equals(level)) return 1;
        return 0;
    }
}
