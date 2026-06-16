package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.BusinessException;
import com.crm.entity.Customer;
import com.crm.entity.FollowUp;
import com.crm.entity.Staff;
import com.crm.mapper.CustomerMapper;
import com.crm.mapper.FollowUpMapper;
import com.crm.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowUpService {

    @Autowired
    private FollowUpMapper followUpMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<FollowUp> listByCustomerId(Integer customerId) {
        return followUpMapper.findByCustomerId(customerId);
    }

    /**
     * 添加跟进记录 — 含完整业务规则校验
     * 规则：
     * 1. 管理员和销售经理不可添加跟进
     * 2. 员工只能跟进自己的客户
     * 3. 不能跟进公海中的客户（salesPerson IS NULL）
     * 4. VIP2/VIP3 客户只能由其专属负责人跟进
     */
    @Transactional
    public boolean add(FollowUp followUp, Staff currentUser) {
        if (followUp.getCustomerId() == null) {
            throw new BusinessException("客户ID不能为空");
        }
        ValidationUtil.requireNotBlank(followUp.getContent(), "跟进内容");

        // 规则1：管理员和销售经理不可添加跟进
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }
        if ("admin".equals(currentUser.getRole()) || "sales_manager".equals(currentUser.getRole())) {
            throw new BusinessException("管理员和销售经理不可添加跟进记录，仅普通员工可操作");
        }

        // 获取客户信息
        Customer customer = customerMapper.selectById(followUp.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        // 规则3：不能跟进公海中的客户
        if (customer.getSalesPerson() == null) {
            throw new BusinessException("不能跟进公海中的客户，请先从公海池领取");
        }

        // 规则2：员工只能跟进自己的客户
        if (!currentUser.getId().equals(customer.getSalesPerson())) {
            throw new BusinessException("只能跟进自己负责的客户");
        }

        // 规则4：VIP2/VIP3 客户只能由其专属负责人跟进
        if ("VIP2".equals(customer.getVipLevel()) || "VIP3".equals(customer.getVipLevel())) {
            if (!currentUser.getId().equals(customer.getSalesPerson())) {
                throw new BusinessException("VIP" + customer.getVipLevel().substring(3) + "客户只能由其专属负责人跟进");
            }
        }

        followUp.setStaffId(currentUser.getId());
        return followUpMapper.insert(followUp) > 0;
    }

    @Transactional
    public boolean delete(Integer id) {
        return followUpMapper.deleteById(id) > 0;
    }

    /** 获取最近跟进记录（用于仪表盘） */
    @Transactional(readOnly = true)
    public List<FollowUp> listRecent(int limit) {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FollowUp::getCreateTime).last("LIMIT " + limit);
        return followUpMapper.selectList(wrapper);
    }

    /** 获取今日跟进数量 */
    @Transactional(readOnly = true)
    public long countToday() {
        LambdaQueryWrapper<FollowUp> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("DATE(create_time) = CURDATE()");
        return followUpMapper.selectCount(wrapper);
    }
}
