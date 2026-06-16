package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.common.BusinessException;
import com.crm.entity.Customer;
import com.crm.entity.Staff;
import com.crm.service.ChurnService;
import com.crm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/churn")
public class ChurnController {

    @Autowired
    private ChurnService churnService;

    @Autowired
    private StaffService staffService;

    private Staff getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("loginUser");
        if (username == null) return null;
        return staffService.findByUsername(username);
    }

    /** 流失客户列表 */
    @GetMapping("/search")
    public ApiResponse<IPage<Customer>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.success(churnService.search(keyword, page, size));
    }

    /** 标记客户为流失 */
    @PutMapping("/mark/{customerId}")
    public ApiResponse<String> markChurn(@PathVariable Integer customerId,
                                          @RequestParam String reason,
                                          HttpServletRequest request) {
        Staff user = getCurrentUser(request);
        if (user == null) throw new BusinessException(401, "请先登录");
        churnService.markChurn(customerId, reason, user.getId());
        return ApiResponse.success("已标记为流失");
    }

    /** 恢复激活 */
    @PutMapping("/restore/{customerId}")
    public ApiResponse<String> restore(@PathVariable Integer customerId, HttpServletRequest request) {
        Staff user = getCurrentUser(request);
        if (user == null) throw new BusinessException(401, "请先登录");
        if (!"admin".equals(user.getRole())) {
            throw new BusinessException(403, "仅管理员可恢复流失客户");
        }
        churnService.restoreActive(customerId);
        return ApiResponse.success("已恢复为活跃状态");
    }
}
