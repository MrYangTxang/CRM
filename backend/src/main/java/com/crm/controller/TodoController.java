package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.common.BusinessException;
import com.crm.entity.Staff;
import com.crm.entity.Todo;
import com.crm.service.StaffService;
import com.crm.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/todo")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private StaffService staffService;

    private Integer getLoginUserId(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("loginUser");
        if (username == null) return null;
        Staff staff = staffService.findByUsername(username);
        return staff != null ? staff.getId() : null;
    }

    @GetMapping("/search")
    public ApiResponse<IPage<Todo>> search(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Integer userId = getLoginUserId(request);
        if (userId == null) throw new BusinessException(401, "请先登录");
        return ApiResponse.success(todoService.search(userId, status, page, size));
    }

    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody Todo todo, HttpServletRequest request) {
        Integer userId = getLoginUserId(request);
        if (userId == null) throw new BusinessException(401, "请先登录");
        todo.setUserId(userId);
        boolean ok = todoService.add(todo);
        return ApiResponse.success(ok ? "添加成功" : "添加失败");
    }

    @PutMapping("/update")
    public ApiResponse<String> update(@RequestBody Todo todo) {
        boolean ok = todoService.update(todo);
        return ApiResponse.success(ok ? "更新成功" : "更新失败");
    }

    @PutMapping("/done/{id}")
    public ApiResponse<String> markDone(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = getLoginUserId(request);
        if (userId == null) throw new BusinessException(401, "请先登录");
        boolean ok = todoService.markDone(id, userId);
        return ApiResponse.success(ok ? "已完成" : "操作失败");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = getLoginUserId(request);
        if (userId == null) throw new BusinessException(401, "请先登录");
        boolean ok = todoService.delete(id, userId);
        return ApiResponse.success(ok ? "删除成功" : "删除失败");
    }
}
