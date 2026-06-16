package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.common.BusinessException;
import com.crm.entity.Staff;
import com.crm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    /** 获取当前登录用户 */
    private Staff getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("loginUser");
        if (username == null) return null;
        return staffService.findByUsername(username);
    }

    /** 校验当前用户是否为管理员 */
    private void requireAdmin(HttpServletRequest request) {
        Staff user = getCurrentUser(request);
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!"admin".equals(user.getRole())) {
            throw new BusinessException(403, "仅管理员可执行此操作");
        }
    }

    /** 校验当前用户是否为管理员或销售经理 */
    private void requireManagerOrAbove(HttpServletRequest request) {
        Staff user = getCurrentUser(request);
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!"admin".equals(user.getRole()) && !"sales_manager".equals(user.getRole())) {
            throw new BusinessException(403, "仅管理员和销售经理可执行此操作");
        }
    }

    @GetMapping("/search")
    public ApiResponse<IPage<Staff>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        requireManagerOrAbove(request);
        return ApiResponse.success(staffService.search(keyword, role, page, size));
    }

    @GetMapping("/list")
    public ApiResponse<List<Staff>> list(HttpServletRequest request) {
        requireManagerOrAbove(request);
        return ApiResponse.success(staffService.listAll());
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<Staff> getById(@PathVariable Integer id, HttpServletRequest request) {
        requireManagerOrAbove(request);
        return ApiResponse.success(staffService.getById(id));
    }

    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody Staff staff, HttpServletRequest request) {
        requireManagerOrAbove(request);
        boolean ok = staffService.add(staff);
        return ApiResponse.success(ok ? "添加成功" : "添加失败");
    }

    @PutMapping("/update")
    public ApiResponse<String> update(@RequestBody Staff staff, HttpServletRequest request) {
        requireManagerOrAbove(request);
        boolean ok = staffService.update(staff);
        return ApiResponse.success(ok ? "更新成功" : "更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id, HttpServletRequest request) {
        requireAdmin(request);
        boolean ok = staffService.delete(id);
        return ApiResponse.success(ok ? "删除成功" : "删除失败");
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestParam String username,
                                                   @RequestParam String password,
                                                   HttpServletRequest request) {
        boolean success = staffService.login(username, password, request);
        if (success) {
            request.getSession().setAttribute("loginUser", username);
            Staff staff = staffService.findByUsername(username);
            Map<String, Object> data = new HashMap<>();
            data.put("role", staff.getRole());
            data.put("username", staff.getUsername());
            return ApiResponse.success("登录成功", data);
        }
        throw new BusinessException(401, "用户名或密码错误");
    }

    @PutMapping("/role/{id}")
    public ApiResponse<String> updateRole(@PathVariable Integer id, @RequestParam String role,
                                          HttpServletRequest request) {
        requireManagerOrAbove(request);
        String currentUsername = (String) request.getSession().getAttribute("loginUser");
        boolean ok = staffService.updateRole(id, role, currentUsername);
        return ApiResponse.success(ok ? "修改成功" : "修改失败");
    }

    @PutMapping("/changePassword")
    public ApiResponse<String> changePassword(@RequestParam String oldPassword,
                                               @RequestParam String newPassword,
                                               HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("loginUser");
        if (username == null) {
            throw new BusinessException(401, "未登录");
        }
        staffService.changePassword(username, oldPassword, newPassword);
        return ApiResponse.success("修改成功");
    }

    @PutMapping("/resetPassword")
    public ApiResponse<String> resetPassword(@RequestParam String username,
                                              @RequestParam String newPassword,
                                              HttpServletRequest request) {
        // 仅管理员可重置他人密码
        requireAdmin(request);
        staffService.resetPassword(username, newPassword);
        return ApiResponse.success("密码重置成功");
    }
}
