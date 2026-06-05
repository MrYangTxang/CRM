package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.entity.WorkOrder;
import com.crm.mapper.WorkOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Transactional
    public boolean createOrder(WorkOrder order) {
        order.setOrderNo("WO" + System.currentTimeMillis());
        order.setStatus("created");
        order.setCreateTime(LocalDateTime.now());
        if (order.getPriority() == null || order.getPriority().trim().isEmpty()) {
            order.setPriority("中");
        }
        return workOrderMapper.insert(order) > 0;
    }

    /** 接单：created/returned/cancelled → processing */
    @Transactional
    public boolean startProcessing(Integer orderId) {
        WorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) return false;
        if (!"created".equals(order.getStatus())
                && !"returned".equals(order.getStatus())
                && !"cancelled".equals(order.getStatus())) {
            throw new BusinessException("当前状态不允许接单");
        }
        return workOrderMapper.updateStatus("processing", orderId) > 0;
    }

    /** 回单：created/processing → returned */
    @Transactional
    public boolean returnOrder(Integer orderId) {
        WorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) return false;
        if (!"created".equals(order.getStatus()) && !"processing".equals(order.getStatus())) {
            throw new BusinessException("只有已创建或处理中的工单才能回单");
        }
        return workOrderMapper.updateStatus("returned", orderId) > 0;
    }

    /** 退单：created/processing → cancelled */
    @Transactional
    public boolean cancelOrder(Integer orderId) {
        WorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) return false;
        if ("completed".equals(order.getStatus())) {
            throw new BusinessException("已完成的工单不能退单");
        }
        return workOrderMapper.updateStatus("cancelled", orderId) > 0;
    }

    /** 完成：processing → completed */
    @Transactional
    public boolean completeOrder(Integer orderId) {
        WorkOrder order = workOrderMapper.selectById(orderId);
        if (order == null) return false;
        if (!"processing".equals(order.getStatus())) {
            throw new BusinessException("只有处理中的工单才能标记为已完成");
        }
        return workOrderMapper.updateStatus("completed", orderId) > 0;
    }

    @Transactional(readOnly = true)
    public List<WorkOrder> listByRegion(String region) {
        return workOrderMapper.findByRegion(region);
    }

    @Transactional(readOnly = true)
    public List<WorkOrder> listAll() {
        return workOrderMapper.selectList(null);
    }

    // ---------------------- 搜索引擎 ----------------------

    @Transactional(readOnly = true)
    public IPage<WorkOrder> search(String keyword, String status, Integer ownerId, boolean isAdmin, Integer page, Integer size) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(WorkOrder::getOrderNo, keyword)
                    .or().like(WorkOrder::getRegion, keyword)
            );
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(WorkOrder::getStatus, status);
        }
        // 数据权限
        if (!isAdmin && ownerId != null) {
            wrapper.eq(WorkOrder::getOwnerId, ownerId);
        }
        wrapper.orderByDesc(WorkOrder::getId);
        return workOrderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /** 待处理工单数（状态为 created/processing） */
    @Transactional(readOnly = true)
    public long countPending(Integer ownerId, boolean isAdmin) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WorkOrder::getStatus, "created", "processing");
        if (!isAdmin && ownerId != null) {
            wrapper.eq(WorkOrder::getOwnerId, ownerId);
        }
        return workOrderMapper.selectCount(wrapper);
    }
}
