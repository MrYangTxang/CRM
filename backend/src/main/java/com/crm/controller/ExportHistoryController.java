package com.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.ApiResponse;
import com.crm.entity.ExportHistory;
import com.crm.service.ExportHistoryService;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/export-history")
public class ExportHistoryController {

    @Autowired
    private ExportHistoryService exportHistoryService;

    @GetMapping("/list")
    public ApiResponse<IPage<ExportHistory>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.success(exportHistoryService.list(page, size));
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        ExportHistory history = exportHistoryService.getById(id);
        if (history == null || history.getFilePath() == null) {
            response.setStatus(404);
            return;
        }
        File file = new File(history.getFilePath());
        if (!file.exists()) {
            response.setStatus(404);
            return;
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(history.getFileName(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);
        try (FileInputStream fis = new FileInputStream(file)) {
            IOUtils.copy(fis, response.getOutputStream());
        }
    }
}
