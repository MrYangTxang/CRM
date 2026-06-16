package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.common.BusinessException;
import com.crm.entity.Customer;
import com.crm.entity.Staff;
import com.crm.service.SeaPoolService;
import com.crm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/seapool")
public class SeaPoolController {

    @Autowired
    private SeaPoolService seaPoolService;

    @Autowired
    private StaffService staffService;

    private Staff getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("loginUser");
        if (username == null) return null;
        return staffService.findByUsername(username);
    }

    /** 公海池列表 */
    @GetMapping("/search")
    public ApiResponse<IPage<Customer>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.success(seaPoolService.search(keyword, page, size));
    }

    /** 领取公海客户 */
    @PostMapping("/claim/{customerId}")
    public ApiResponse<String> claim(@PathVariable Integer customerId, HttpServletRequest request) {
        Staff user = getCurrentUser(request);
        if (user == null) throw new BusinessException(401, "请先登录");
        seaPoolService.claim(customerId, user.getId());
        return ApiResponse.success("领取成功");
    }

    /** 退回公海（管理员/经理） */
    @PutMapping("/return/{customerId}")
    public ApiResponse<String> returnToPool(@PathVariable Integer customerId, HttpServletRequest request) {
        Staff user = getCurrentUser(request);
        if (user == null) throw new BusinessException(401, "请先登录");
        if (!"admin".equals(user.getRole()) && !"sales_manager".equals(user.getRole())) {
            throw new BusinessException(403, "仅管理员和销售经理可退回公海");
        }
        seaPoolService.returnToPool(customerId);
        return ApiResponse.success("已退回公海");
    }

    /** 剩余可领取额度 */
    @GetMapping("/quota")
    public ApiResponse<Integer> remainingQuota(HttpServletRequest request) {
        Staff user = getCurrentUser(request);
        if (user == null) throw new BusinessException(401, "请先登录");
        return ApiResponse.success(seaPoolService.remainingQuota(user.getId()));
    }
}
