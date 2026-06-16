package com.crm.controller;

import com.crm.common.ApiResponse;
import com.crm.entity.Region;
import com.crm.entity.SysConfig;
import com.crm.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sysconfig")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    // ==================== 系统参数 ====================

    @GetMapping("/configs")
    public ApiResponse<List<SysConfig>> listConfigs() {
        return ApiResponse.success(sysConfigService.listConfigs());
    }

    @PostMapping("/config")
    public ApiResponse<String> addConfig(@RequestBody SysConfig config) {
        boolean ok = sysConfigService.addConfig(config);
        return ApiResponse.success(ok ? "添加成功" : "添加失败");
    }

    @PutMapping("/config")
    public ApiResponse<String> updateConfig(@RequestBody SysConfig config) {
        boolean ok = sysConfigService.updateConfig(config);
        return ApiResponse.success(ok ? "更新成功" : "更新失败");
    }

    @DeleteMapping("/config/{id}")
    public ApiResponse<String> deleteConfig(@PathVariable Integer id) {
        boolean ok = sysConfigService.deleteConfig(id);
        return ApiResponse.success(ok ? "删除成功" : "删除失败");
    }

    // ==================== 区域管理 ====================

    @GetMapping("/regions")
    public ApiResponse<List<Region>> listRegions() {
        return ApiResponse.success(sysConfigService.listRegions());
    }

    @GetMapping("/region-tree")
    public ApiResponse<List<Map<String, Object>>> regionTree() {
        return ApiResponse.success(sysConfigService.regionTree());
    }

    @PostMapping("/region")
    public ApiResponse<String> addRegion(@RequestBody Region region) {
        boolean ok = sysConfigService.addRegion(region);
        return ApiResponse.success(ok ? "添加成功" : "添加失败");
    }

    @PutMapping("/region")
    public ApiResponse<String> updateRegion(@RequestBody Region region) {
        boolean ok = sysConfigService.updateRegion(region);
        return ApiResponse.success(ok ? "更新成功" : "更新失败");
    }

    @DeleteMapping("/region/{id}")
    public ApiResponse<String> deleteRegion(@PathVariable Integer id) {
        boolean ok = sysConfigService.deleteRegion(id);
        return ApiResponse.success(ok ? "删除成功" : "删除失败");
    }
}
