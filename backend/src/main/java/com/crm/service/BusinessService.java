package com.crm.service;

import com.crm.entity.Business;
import com.crm.mapper.BusinessMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;


@Service
public class BusinessService {

    @Autowired
    private BusinessMapper businessMapper;

    @Transactional(readOnly = true)
    public List<Business> listAll() {
        return businessMapper.findAll();
    }

    @Transactional(readOnly = true)
    public Business getById(Integer id) {
        return businessMapper.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Business> listByCustomerId(Integer customerId) {
        return businessMapper.findByCustomerId(customerId);
    }

    @Transactional
    public boolean add(Business business) {
        return businessMapper.insert(business) > 0;
    }

    @Transactional
    public boolean update(Business business) {
        return businessMapper.update(business) > 0;
    }

    @Transactional
    public boolean delete(Integer id) {
        return businessMapper.deleteById(id) > 0;
    }
    // 导出 Excel
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Business> list = businessMapper.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("业务列表");
        // 表头
        Row header = sheet.createRow(0);
        String[] headers = {"ID", "业务名称", "类型", "金额", "状态", "客户ID"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
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
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("业务数据_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // 导入 Excel
    @Transactional
    public void importFromExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Business business = new Business();
            business.setName(getCellValue(row.getCell(1)));
            business.setType(getCellValue(row.getCell(2)));
            String priceStr = getCellValue(row.getCell(3));
            if (!priceStr.isEmpty()) business.setPrice(new java.math.BigDecimal(priceStr));
            business.setStatus(getCellValue(row.getCell(4)));
            String cidStr = getCellValue(row.getCell(5));
            if (!cidStr.isEmpty()) business.setCustomerId(Integer.parseInt(cidStr));
            businessMapper.insert(business);
        }
        workbook.close();
    }
    // 辅助方法：获取单元格字符串值
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    // 辅助方法：表头样式（可选）
    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}