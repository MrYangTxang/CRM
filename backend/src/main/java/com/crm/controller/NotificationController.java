package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.common.BusinessException;
import com.crm.entity.Notification;
import com.crm.entity.Staff;
import com.crm.service.NotificationService;
import com.crm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StaffService staffService;

    private Integer getLoginUserId(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("loginUser");
        if (username == null) return null;
        Staff staff = staffService.findByUsername(username);
        return staff != null ? staff.getId() : null;
    }

    @GetMapping("/list")
    public ApiResponse<IPage<Notification>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Integer userId = getLoginUserId(request);
        if (userId == null) throw new BusinessException(401, "请先登录");
        return ApiResponse.success(notificationService.list(userId, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(HttpServletRequest request) {
        Integer userId = getLoginUserId(request);
        if (userId == null) return ApiResponse.success(0L);
        return ApiResponse.success(notificationService.unreadCount(userId));
    }

    @PutMapping("/read/{id}")
    public ApiResponse<String> markRead(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = getLoginUserId(request);
        if (userId == null) throw new BusinessException(401, "请先登录");
        notificationService.markRead(id, userId);
        return ApiResponse.success("已读");
    }

    @PutMapping("/read-all")
    public ApiResponse<String> markAllRead(HttpServletRequest request) {
        Integer userId = getLoginUserId(request);
        if (userId == null) throw new BusinessException(401, "请先登录");
        notificationService.markAllRead(userId);
        return ApiResponse.success("全部已读");
    }
}
