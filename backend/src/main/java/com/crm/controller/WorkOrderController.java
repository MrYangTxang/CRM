package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.entity.WorkOrder;
import com.crm.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/workorder")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    private String getLoginUsername(HttpServletRequest request) {
        Object loginUser = request.getSession().getAttribute("loginUser");
        return loginUser != null ? loginUser.toString() : null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        return "admin".equals(getLoginUsername(request));
    }

    @PostMapping("/create")
    public ApiResponse<String> create(@RequestBody WorkOrder order) {
        boolean ok = workOrderService.createOrder(order);
        return ApiResponse.success(ok ? "工单创建成功" : "创建失败");
    }

    /** 接单：→ processing */
    @PutMapping("/start/{id}")
    public ApiResponse<String> startProcessing(@PathVariable Integer id) {
        boolean ok = workOrderService.startProcessing(id);
        return ApiResponse.success(ok ? "接单成功" : "接单失败");
    }

    @PutMapping("/return/{id}")
    public ApiResponse<String> returnOrder(@PathVariable Integer id) {
        boolean ok = workOrderService.returnOrder(id);
        return ApiResponse.success(ok ? "回单成功" : "回单失败");
    }

    @PutMapping("/cancel/{id}")
    public ApiResponse<String> cancelOrder(@PathVariable Integer id) {
        boolean ok = workOrderService.cancelOrder(id);
        return ApiResponse.success(ok ? "退单成功" : "退单失败");
    }

    /** 完成工单 */
    @PutMapping("/complete/{id}")
    public ApiResponse<String> completeOrder(@PathVariable Integer id) {
        boolean ok = workOrderService.completeOrder(id);
        return ApiResponse.success(ok ? "工单已完成" : "操作失败");
    }

    @GetMapping("/region/{region}")
    public ApiResponse<List<WorkOrder>> listByRegion(@PathVariable String region) {
        return ApiResponse.success(workOrderService.listByRegion(region));
    }

    @GetMapping("/search")
    public ApiResponse<IPage<WorkOrder>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        return ApiResponse.success(workOrderService.search(keyword, status, null, isAdmin(request), page, size));
    }

    @GetMapping("/list")
    public ApiResponse<List<WorkOrder>> list() {
        return ApiResponse.success(workOrderService.listAll());
    }

    /** 待处理工单数 */
    @GetMapping("/pending-count")
    public ApiResponse<Long> pendingCount(HttpServletRequest request) {
        return ApiResponse.success(workOrderService.countPending(null, isAdmin(request)));
    }
}
