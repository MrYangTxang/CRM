package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.entity.Business;
import com.crm.mapper.BusinessMapper;
import com.crm.util.ExcelUtil;
import com.crm.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;

@Slf4j
@Service
public class BusinessService {

    @Autowired
    private BusinessMapper businessMapper;

    @Transactional(readOnly = true)
    public List<Business> listAll() {
        return businessMapper.selectList(null);
    }

    @Transactional(readOnly = true)
    public Business getById(Integer id) {
        return businessMapper.selectById(id);
    }

    @Transactional(readOnly = true)
    public List<Business> listByCustomerId(Integer customerId) {
        return businessMapper.findByCustomerId(customerId);
    }

    @Transactional
    public boolean add(Business business) {
        validateBusiness(business);
        return businessMapper.insert(business) > 0;
    }

    @Transactional
    public boolean update(Business business) {
        validateBusiness(business);
        return businessMapper.updateById(business) > 0;
    }

    @Transactional
    public boolean delete(Integer id) {
        return businessMapper.deleteById(id) > 0;
    }

    // ---------------------- 搜索引擎（含数据权限）----------------------

    @Transactional(readOnly = true)
    public IPage<Business> search(String keyword, String status, Integer customerId,
                                   Integer ownerId, boolean isAdmin, Integer page, Integer size) {
        log.info("[BusinessService] search 入参: keyword={}, status={}, customerId={}, ownerId={}, isAdmin={}, page={}, size={}",
                keyword, status, customerId, ownerId, isAdmin, page, size);
        LambdaQueryWrapper<Business> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Business::getName, keyword)
                    .or().like(Business::getType, keyword)
            );
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Business::getStatus, status);
        }
        if (customerId != null) {
            wrapper.eq(Business::getCustomerId, customerId);
        }
        // 数据权限
        if (!isAdmin && ownerId != null) {
            wrapper.eq(Business::getOwnerId, ownerId);
        }
        wrapper.orderByAsc(Business::getId);
        IPage<Business> result = businessMapper.selectPage(new Page<>(page, size), wrapper);
        log.info("[BusinessService] search 结果: total={}, records.size={}", result.getTotal(),
                result.getRecords() != null ? result.getRecords().size() : 0);
        return result;
    }

    private void validateBusiness(Business business) {
        ValidationUtil.requireNotBlank(business.getName(), "业务名称");
        if (business.getPrice() != null && business.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("业务金额必须大于0");
        }
        if (business.getStatus() != null && !business.getStatus().matches("进行中|已完成|暂停")) {
            throw new BusinessException("业务状态必须是'进行中'、'已完成'或'暂停'");
        }
    }

    // ---------------------- Excel 导入导出 ----------------------
    public void exportToExcel(HttpServletResponse response, List<Business> list) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("业务列表");
        Row header = sheet.createRow(0);
        String[] headers = {"ID", "业务名称", "类型", "金额", "状态", "客户ID", "负责人ID"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(ExcelUtil.createHeaderStyle(workbook));
        }
        int rowNum = 1;
        for (Business b : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(b.getId());
            row.createCell(1).setCellValue(b.getName());
            row.createCell(2).setCellValue(b.getType() != null ? b.getType() : "");
            row.createCell(3).setCellValue(b.getPrice() != null ? b.getPrice().doubleValue() : 0);
            row.createCell(4).setCellValue(b.getStatus() != null ? b.getStatus() : "");
            row.createCell(5).setCellValue(b.getCustomerId() != null ? b.getCustomerId() : 0);
            row.createCell(6).setCellValue(b.getOwnerId() != null ? b.getOwnerId() : 0);
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("业务数据_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @Transactional
    public void importFromExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Business business = new Business();
            business.setName(ExcelUtil.getCellValue(row.getCell(1)));
            business.setType(ExcelUtil.getCellValue(row.getCell(2)));
            String priceStr = ExcelUtil.getCellValue(row.getCell(3));
            if (!priceStr.isEmpty()) business.setPrice(new BigDecimal(priceStr));
            business.setStatus(ExcelUtil.getCellValue(row.getCell(4)));
            String cidStr = ExcelUtil.getCellValue(row.getCell(5));
            if (!cidStr.isEmpty()) business.setCustomerId(Integer.parseInt(cidStr));
            String oidStr = ExcelUtil.getCellValue(row.getCell(6));
            if (!oidStr.isEmpty()) business.setOwnerId(Integer.parseInt(oidStr));
            businessMapper.insert(business);
        }
        workbook.close();
    }
}
