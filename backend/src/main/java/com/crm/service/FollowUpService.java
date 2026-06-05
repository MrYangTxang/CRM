package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.BusinessException;
import com.crm.entity.FollowUp;
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

    @Transactional(readOnly = true)
    public List<FollowUp> listByCustomerId(Integer customerId) {
        return followUpMapper.findByCustomerId(customerId);
    }

    @Transactional
    public boolean add(FollowUp followUp) {
        if (followUp.getCustomerId() == null) {
            throw new BusinessException("客户ID不能为空");
        }
        ValidationUtil.requireNotBlank(followUp.getContent(), "跟进内容");
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
