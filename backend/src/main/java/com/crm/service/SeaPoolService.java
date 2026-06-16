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

import java.time.LocalDate;
import java.util.List;

/**
 * 公海池服务：客户 salesPerson IS NULL 即为公海客户
 */
@Slf4j
@Service
public class SeaPoolService {

    @Autowired
    private CustomerMapper customerMapper;

    /** 公海池列表（分页搜索） */
    @Transactional(readOnly = true)
    public IPage<Customer> search(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Customer::getSalesPerson)
               .eq(Customer::getDeleted, 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Customer::getName, keyword)
                    .or().like(Customer::getPhone, keyword)
            );
        }
        wrapper.orderByAsc(Customer::getId);
        return customerMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /** 领取公海客户（每日上限3个） */
    @Transactional
    public void claim(Integer customerId, Integer userId) {
        // 校验每日领取上限
        long todayClaimed = countTodayClaims(userId);
        if (todayClaimed >= 3) {
            throw new BusinessException("今日已领取3个公海客户，已达每日上限");
        }

        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        if (customer.getDeleted() != null && customer.getDeleted() == 1) {
            throw new BusinessException("该客户已被删除");
        }
        if (customer.getSalesPerson() != null) {
            throw new BusinessException("该客户已被他人领取");
        }
        if ("churned".equals(customer.getStatus())) {
            throw new BusinessException("该客户已流失，无法领取");
        }

        customer.setSalesPerson(userId);
        customerMapper.updateById(customer);
        log.info("用户{}领取公海客户: id={}, name={}", userId, customerId, customer.getName());
    }

    /** 退回公海（管理员/经理权限） */
    @Transactional
    public void returnToPool(Integer customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        customer.setSalesPerson(null);
        customerMapper.updateById(customer);
        log.info("客户{}已退回公海: name={}", customerId, customer.getName());
    }

    /** 今日剩余可领取数 */
    @Transactional(readOnly = true)
    public int remainingQuota(Integer userId) {
        long today = countTodayClaims(userId);
        return Math.max(0, 3 - (int) today);
    }

    /** 统计今日领取数（通过查询今天更新了 salesPerson 的客户数） */
    private long countTodayClaims(Integer userId) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getSalesPerson, userId)
               .apply("DATE(update_time) = CURDATE()");
        return customerMapper.selectCount(wrapper);
    }
}
