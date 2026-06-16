package com.crm.controller;

import com.crm.common.ApiResponse;
import com.crm.common.BusinessException;
import com.crm.entity.FollowUp;
import com.crm.entity.Staff;
import com.crm.service.FollowUpService;
import com.crm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/followup")
public class FollowUpController {

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private StaffService staffService;

    /** 获取当前登录用户 */
    private Staff getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("loginUser");
        if (username == null) return null;
        return staffService.findByUsername(username);
    }

    /** 获取某客户的所有跟进记录 */
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<FollowUp>> listByCustomer(@PathVariable Integer customerId) {
        return ApiResponse.success(followUpService.listByCustomerId(customerId));
    }

    /** 添加跟进记录 — 含角色/归属/公海/VIP校验 */
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody FollowUp followUp, HttpServletRequest request) {
        Staff currentUser = getCurrentUser(request);
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }
        boolean ok = followUpService.add(followUp, currentUser);
        return ApiResponse.success(ok ? "添加成功" : "添加失败");
    }

    /** 删除跟进记录 */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        boolean ok = followUpService.delete(id);
        return ApiResponse.success(ok ? "删除成功" : "删除失败");
    }
}
