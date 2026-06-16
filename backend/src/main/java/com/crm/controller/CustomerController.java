package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.entity.Customer;
import com.crm.entity.ExportHistory;
import com.crm.entity.Staff;
import com.crm.service.CustomerService;
import com.crm.service.ExportHistoryService;
import com.crm.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ExportHistoryService exportHistoryService;

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

    /** 获取当前登录用户的ID */
    private Integer getLoginUserId(HttpServletRequest request) {
        String username = getLoginUsername(request);
        if (username != null) {
            Staff staff = staffService.findByUsername(username);
            if (staff != null) return staff.getId();
        }
        return null;
    }

    @GetMapping("/search")
    public ApiResponse<IPage<Customer>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vipLevel,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        boolean admin = isAdmin(request);
        Integer salesPerson = admin ? null : getLoginUserId(request);
        return ApiResponse.success(customerService.search(keyword, vipLevel, salesPerson, admin, page, size));
    }

    @GetMapping("/list")
    public ApiResponse<List<Customer>> list() {
        return ApiResponse.success(customerService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Customer> getById(@PathVariable Integer id) {
        return ApiResponse.success(customerService.getById(id));
    }

    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody Customer customer) {
        boolean ok = customerService.add(customer);
        return ApiResponse.success(ok ? "添加成功" : "添加失败");
    }

    @PutMapping("/update")
    public ApiResponse<String> update(@RequestBody Customer customer) {
        boolean ok = customerService.update(customer);
        return ApiResponse.success(ok ? "更新成功" : "更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        boolean ok = customerService.delete(id);
        return ApiResponse.success(ok ? "删除成功" : "删除失败");
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response, HttpServletRequest request) throws IOException {
        // 按数据权限导出，而非导出全部
        boolean admin = isAdmin(request);
        Integer salesPerson = admin ? null : getLoginUserId(request);
        List<Customer> customers = customerService.searchAll(null, null, salesPerson, admin);
        customerService.exportToExcel(response, customers);
        ExportHistory history = new ExportHistory();
        history.setTableName("customer");
        history.setFileName("客户数据_" + System.currentTimeMillis() + ".xlsx");
        history.setRecordCount(customers.size());
        history.setUsername(getLoginUsername(request));
        history.setCreateTime(LocalDateTime.now());
        exportHistoryService.save(history);
    }

    @PostMapping("/import")
    public ApiResponse<String> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        CustomerService.ImportResult result = customerService.importFromExcel(file);
        if (result.hasErrors()) {
            return ApiResponse.success(String.format(
                    "导入完成：成功 %d 条，跳过 %d 条（含重复）。错误详情：%s",
                    result.getInserted(), result.getSkipped(),
                    String.join("; ", result.getErrors())));
        }
        return ApiResponse.success(String.format("导入完成：成功 %d 条，跳过 %d 条", result.getInserted(), result.getSkipped()));
    }

    /** 省份/城市统计 */
    @GetMapping("/region-stats")
    public ApiResponse<List<Map<String, Object>>> regionStats() {
        List<Customer> customers = customerService.listAllForStats();
        Map<String, Integer> provinceCount = new HashMap<>();
        Pattern pattern = Pattern.compile("^(.+?)(?:省|市)");
        for (Customer c : customers) {
            String addr = c.getAddress();
            if (addr == null || addr.trim().isEmpty()) continue;
            Matcher m = pattern.matcher(addr);
            if (m.find()) {
                String province = m.group(1) + (addr.contains("省") ? "省" : "市");
                provinceCount.merge(province, 1, Integer::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : provinceCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            result.add(item);
        }
        result.sort((a, b) -> ((Integer) b.get("value")).compareTo((Integer) a.get("value")));
        return ApiResponse.success(result);
    }
}
