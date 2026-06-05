package com.crm.controller;

import com.crm.common.ApiResponse;
import com.crm.entity.FollowUp;
import com.crm.service.FollowUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/followup")
public class FollowUpController {

    @Autowired
    private FollowUpService followUpService;

    /** 获取某客户的所有跟进记录 */
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<FollowUp>> listByCustomer(@PathVariable Integer customerId) {
        return ApiResponse.success(followUpService.listByCustomerId(customerId));
    }

    /** 添加跟进记录 */
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody FollowUp followUp) {
        boolean ok = followUpService.add(followUp);
        return ApiResponse.success(ok ? "添加成功" : "添加失败");
    }

    /** 删除跟进记录 */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Integer id) {
        boolean ok = followUpService.delete(id);
        return ApiResponse.success(ok ? "删除成功" : "删除失败");
    }
}
