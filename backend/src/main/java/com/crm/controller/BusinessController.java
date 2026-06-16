package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.entity.Business;
import com.crm.entity.ExportHistory;
import com.crm.entity.Staff;
import com.crm.service.BusinessService;
import com.crm.service.ExportHistoryService;
import com.crm.service.StaffService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private ExportHistoryService exportHistoryService;

    @Autowired
    private StaffService staffService;

    private String getLoginUsername(HttpServletRequest request) {
        Object loginUser = request.getSession().getAttribute("loginUser");
        return loginUser != null ? loginUser.toString() : null;
    }

    /** 获取当前登录用户ID */
    private Integer getLoginUserId(HttpServletRequest request) {
        String username = getLoginUsername(request);
        if (username != null) {
            Staff staff = staffService.findByUsername(username);
            if (staff != null) return staff.getId();
        }
        return null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        String username = getLoginUsername(request);
        if (username == null) return false;
        Staff staff = staffService.findByUsername(username);
        return staff != null && "admin".equals(staff.getRole());
    }

    @GetMapping("/search")
    public ApiResponse<IPage<Business>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        boolean admin = isAdmin(request);
        Integer ownerId = admin ? null : getLoginUserId(request);
        return ApiResponse.success(businessService.search(keyword, status, customerId, ownerId, admin, page, size));
    }

    @GetMapping("/list")
    public ApiResponse<List<Business>> list() {
        return ApiResponse.success(businessService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Business> get(@PathVariable Integer id) {
        return ApiResponse.success(businessService.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<Business>> listByCustomer(@PathVariable Integer customerId) {
        return ApiResponse.success(businessService.listByCustomerId(customerId));
    }

    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody Business business) {
        boolean ok = businessService.add(business);
        return ApiResponse.success(ok ? "添加成功" : "添加失败");
    }

    @PutMapping("/update")
    public ApiResponse<String> update(@RequestBody Business business) {
        boolean ok = businessService.update(business);
        return ApiResponse.success(ok ? "更新成功" : "更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        boolean ok = businessService.delete(id);
        return ApiResponse.success(ok ? "删除成功" : "删除失败");
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response, HttpServletRequest request) throws IOException {
        // 按数据权限导出，而非导出全部
        boolean admin = isAdmin(request);
        Integer ownerId = admin ? null : getLoginUserId(request);
        List<Business> list = businessService.searchAll(null, null, null, ownerId, admin);
        businessService.exportToExcel(response, list);
        // 记录导出历史
        ExportHistory history = new ExportHistory();
        history.setTableName("business");
        history.setFileName("业务数据_" + System.currentTimeMillis() + ".xlsx");
        history.setRecordCount(list.size());
        history.setUsername(getLoginUsername(request));
        history.setCreateTime(LocalDateTime.now());
        exportHistoryService.save(history);
    }

    @PostMapping("/import")
    public ApiResponse<String> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        BusinessService.ImportResult result = businessService.importFromExcel(file);
        if (result.hasErrors()) {
            return ApiResponse.success(String.format(
                    "导入完成：成功 %d 条，跳过 %d 条（含重复）。错误详情：%s",
                    result.getInserted(), result.getSkipped(),
                    String.join("; ", result.getErrors())));
        }
        return ApiResponse.success(String.format("导入完成：成功 %d 条，跳过 %d 条", result.getInserted(), result.getSkipped()));
    }
}
