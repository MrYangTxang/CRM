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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BusinessService {

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private VipService vipService;

    // ---------------------- 基础 CRUD ----------------------

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
        boolean ok = businessMapper.insert(business) > 0;
        if (ok && business.getCustomerId() != null) {
            vipService.autoUpgrade(business.getCustomerId());
        }
        return ok;
    }

    @Transactional
    public boolean update(Business business) {
        validateBusiness(business);
        boolean ok = businessMapper.updateById(business) > 0;
        if (ok && business.getCustomerId() != null) {
            vipService.autoUpgrade(business.getCustomerId());
        }
        return ok;
    }

    @Transactional
    public boolean delete(Integer id) {
        return businessMapper.deleteById(id) > 0;
    }

    @Transactional
    public int batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return businessMapper.deleteBatchIds(ids);
    }

    // ---------------------- 搜索引擎（含数据权限）----------------------

    @Transactional(readOnly = true)
    public IPage<Business> search(String keyword, String status, Integer customerId,
                                   Integer ownerId, boolean isAdmin, Integer page, Integer size) {
        log.debug("Business search: keyword={}, status={}, customerId={}, ownerId={}, isAdmin={}",
                keyword, status, customerId, ownerId, isAdmin);
        LambdaQueryWrapper<Business> wrapper = buildSearchWrapper(keyword, status, customerId, ownerId, isAdmin);
        wrapper.orderByAsc(Business::getId);
        return businessMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /** 按权限获取全部业务（用于导出），不分页 */
    @Transactional(readOnly = true)
    public List<Business> searchAll(String keyword, String status, Integer customerId,
                                    Integer ownerId, boolean isAdmin) {
        LambdaQueryWrapper<Business> wrapper = buildSearchWrapper(keyword, status, customerId, ownerId, isAdmin);
        wrapper.orderByAsc(Business::getId);
        return businessMapper.selectList(wrapper);
    }

    private LambdaQueryWrapper<Business> buildSearchWrapper(String keyword, String status,
                                                             Integer customerId, Integer ownerId, boolean isAdmin) {
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
        // 数据权限：非管理员只能看自己的业务
        if (!isAdmin && ownerId != null) {
            wrapper.eq(Business::getOwnerId, ownerId);
        }
        return wrapper;
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

    // ---------------------- Excel 导出 ----------------------

    public void exportToExcel(HttpServletResponse response, List<Business> list) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
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
                row.createCell(0).setCellValue(b.getId() != null ? b.getId() : 0);
                row.createCell(1).setCellValue(b.getName() != null ? b.getName() : "");
                row.createCell(2).setCellValue(b.getType() != null ? b.getType() : "");
                row.createCell(3).setCellValue(b.getPrice() != null ? b.getPrice().doubleValue() : 0);
                row.createCell(4).setCellValue(b.getStatus() != null ? b.getStatus() : "");
                row.createCell(5).setCellValue(b.getCustomerId() != null ? b.getCustomerId() : 0);
                row.createCell(6).setCellValue(b.getOwnerId() != null ? b.getOwnerId() : 0);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("业务数据_" + System.currentTimeMillis(), "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            workbook.write(response.getOutputStream());
        }
    }

    // ---------------------- Excel 导入（含去重 + 校验）-----------------------

    @Transactional
    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        int total = 0, skipped = 0, inserted = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                total++;
                Row row = sheet.getRow(i);
                if (row == null) {
                    skipped++;
                    continue;
                }

                try {
                    String name = ExcelUtil.getCellValue(row.getCell(1));
                    String type = ExcelUtil.getCellValue(row.getCell(2));
                    String priceStr = ExcelUtil.getCellValue(row.getCell(3));
                    String status = ExcelUtil.getCellValue(row.getCell(4));
                    String cidStr = ExcelUtil.getCellValue(row.getCell(5));
                    String oidStr = ExcelUtil.getCellValue(row.getCell(6));

                    // 跳过完全空行
                    if (name.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    Integer customerId = cidStr.isEmpty() ? null : Integer.parseInt(cidStr);

                    // 去重：名称 + 客户ID 已存在则跳过
                    if (customerId != null) {
                        Business existing = businessMapper.findByNameAndCustomerId(name, customerId);
                        if (existing != null) {
                            log.debug("导入跳过重复行[{}]: name={}, customerId={}", i + 1, name, customerId);
                            skipped++;
                            continue;
                        }
                    }

                    Business business = new Business();
                    business.setName(name);
                    business.setType(type);
                    if (!priceStr.isEmpty()) {
                        business.setPrice(new BigDecimal(priceStr));
                    }
                    business.setStatus(status);
                    business.setCustomerId(customerId);
                    if (!oidStr.isEmpty()) {
                        business.setOwnerId(Integer.parseInt(oidStr));
                    }

                    // 数据校验
                    validateBusiness(business);
                    businessMapper.insert(business);
                    inserted++;
                } catch (NumberFormatException e) {
                    errors.add("第" + (i + 1) + "行数字格式错误: " + e.getMessage());
                    skipped++;
                } catch (BusinessException e) {
                    errors.add("第" + (i + 1) + "行校验失败: " + e.getMessage());
                    skipped++;
                }
            }
        }

        log.info("业务导入完成: 总计{}行, 插入{}, 跳过{}, 错误{}条",
                total, inserted, skipped, errors.size());
        if (!errors.isEmpty()) {
            log.warn("导入异常明细: {}", errors);
        }

        ImportResult result = new ImportResult();
        result.setTotal(total);
        result.setInserted(inserted);
        result.setSkipped(skipped);
        result.setErrors(errors);
        return result;
    }

    /** 导入结果 DTO */
    public static class ImportResult {
        private int total;
        private int inserted;
        private int skipped;
        private List<String> errors;

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getInserted() { return inserted; }
        public void setInserted(int inserted) { this.inserted = inserted; }
        public int getSkipped() { return skipped; }
        public void setSkipped(int skipped) { this.skipped = skipped; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }

        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }
}
