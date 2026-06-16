package com.crm.controller;

import com.crm.common.ApiResponse;
import com.crm.service.RecycleBinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recyclebin")
public class RecycleBinController {

    @Autowired
    private RecycleBinService recycleBinService;

    /** 列出指定表的软删除记录 */
    @GetMapping("/list/{tableName}")
    public ApiResponse<List<Map<String, Object>>> listDeleted(@PathVariable String tableName) {
        return ApiResponse.success(recycleBinService.listDeleted(tableName));
    }

    /** 各表统计 */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.success(recycleBinService.stats());
    }

    /** 恢复 */
    @PutMapping("/restore/{tableName}/{recordId}")
    public ApiResponse<String> restore(@PathVariable String tableName, @PathVariable Integer recordId) {
        recycleBinService.restore(tableName, recordId);
        return ApiResponse.success("恢复成功");
    }

    /** 彻底删除 */
    @DeleteMapping("/permanent/{tableName}/{recordId}")
    public ApiResponse<String> permanentDelete(@PathVariable String tableName, @PathVariable Integer recordId) {
        recycleBinService.permanentDelete(tableName, recordId);
        return ApiResponse.success("彻底删除成功");
    }
}
