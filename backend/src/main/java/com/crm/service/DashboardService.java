package com.crm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘服务：聚合统计各模块数据
 */
@Service
public class DashboardService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private FollowUpService followUpService;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(Integer userId, boolean isAdmin) {
        Map<String, Object> data = new HashMap<>();

        // 客户总数
        long customerCount = customerService.listAll().size();
        data.put("customerCount", customerCount);

        // 待处理工单数
        long pendingOrders = workOrderService.countPending(userId, isAdmin);
        data.put("pendingOrderCount", pendingOrders);

        // 今日跟进数
        long todayFollowUps = followUpService.countToday();
        data.put("todayFollowUpCount", todayFollowUps);

        // 最近10条跟进记录
        data.put("recentFollowUps", followUpService.listRecent(10));

        return data;
    }
}
