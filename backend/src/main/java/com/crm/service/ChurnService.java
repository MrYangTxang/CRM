package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.entity.Customer;
import com.crm.mapper.CustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 流失管理服务
 */
@Slf4j
@Service
public class ChurnService {

    @Autowired
    private CustomerMapper customerMapper;

    /** 流失客户列表 */
    @Transactional(readOnly = true)
    public IPage<Customer> search(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getStatus, "churned")
               .eq(Customer::getDeleted, 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Customer::getName, keyword)
                    .or().like(Customer::getPhone, keyword)
            );
        }
        wrapper.orderByDesc(Customer::getChurnTime);
        return customerMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /** 标记客户为流失 */
    @Transactional
    public void markChurn(Integer customerId, String reason, Integer operatorId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        if ("churned".equals(customer.getStatus())) {
            throw new BusinessException("该客户已是流失状态");
        }
        // 校验：只有当前负责人可以标记流失
        if (customer.getSalesPerson() != null && !customer.getSalesPerson().equals(operatorId)) {
            throw new BusinessException("只有客户负责人才能标记流失");
        }

        customer.setStatus("churned");
        customer.setChurnReason(reason);
        customer.setChurnTime(LocalDateTime.now());
        customerMapper.updateById(customer);
        log.info("客户已标记为流失: id={}, name={}, reason={}", customerId, customer.getName(), reason);
    }

    /** 恢复激活（将流失客户恢复为active） */
    @Transactional
    public void restoreActive(Integer customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        if (!"churned".equals(customer.getStatus())) {
            throw new BusinessException("该客户不是流失状态，无需恢复");
        }

        customer.setStatus("active");
        customer.setChurnReason(null);
        customer.setChurnTime(null);
        customerMapper.updateById(customer);
        log.info("流失客户已恢复: id={}, name={}", customerId, customer.getName());
    }
}
