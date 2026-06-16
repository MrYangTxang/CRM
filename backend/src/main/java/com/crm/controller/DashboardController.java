package com.crm.controller;

import com.crm.common.ApiResponse;
import com.crm.entity.Staff;
import com.crm.service.DashboardService;
import com.crm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private StaffService staffService;

    private String getLoginUsername(HttpServletRequest request) {
        Object loginUser = request.getSession().getAttribute("loginUser");
        return loginUser != null ? loginUser.toString() : null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        String username = getLoginUsername(request);
        if (username == null) return false;
        Staff staff = staffService.findByUsername(username);
        return staff != null && "admin".equals(staff.getRole());
    }

    private Integer getLoginUserId(HttpServletRequest request) {
        String username = getLoginUsername(request);
        if (username != null) {
            Staff staff = staffService.findByUsername(username);
            if (staff != null) return staff.getId();
        }
        return null;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> dashboard(HttpServletRequest request) {
        boolean admin = isAdmin(request);
        Integer userId = admin ? null : getLoginUserId(request);
        return ApiResponse.success(dashboardService.getDashboard(userId, admin));
    }
}
