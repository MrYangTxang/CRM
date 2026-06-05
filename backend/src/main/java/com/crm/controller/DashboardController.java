package com.crm.controller;

import com.crm.common.ApiResponse;
import com.crm.service.DashboardService;
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

    private boolean isAdmin(HttpServletRequest request) {
        Object loginUser = request.getSession().getAttribute("loginUser");
        return "admin".equals(loginUser != null ? loginUser.toString() : null);
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> dashboard(HttpServletRequest request) {
        return ApiResponse.success(dashboardService.getDashboard(null, isAdmin(request)));
    }
}
