package com.crm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.ApiResponse;
import com.crm.entity.OperationLog;
import com.crm.mapper.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @GetMapping("/list")
    public ApiResponse<IPage<OperationLog>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return ApiResponse.success(operationLogMapper.selectPage(new Page<>(page, size), wrapper));
    }
}
