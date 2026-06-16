package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.entity.Customer;
import com.crm.mapper.CustomerMapper;
import com.crm.util.ExcelUtil;
import com.crm.util.ValidationUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<Customer> listAll() {
        return customerMapper.selectList(null);
    }

    @Transactional(readOnly = true)
    public Customer getById(Integer id) {
        return customerMapper.selectById(id);
    }

    @Transactional
    public boolean add(Customer customer) {
        validateCustomer(customer);
        return customerMapper.insert(customer) > 0;
    }

    @Transactional
    public boolean update(Customer customer) {
        validateCustomer(customer);
        return customerMapper.updateById(customer) > 0;
    }

    @Transactional
    public boolean delete(Integer id) {
        // 软删除：设置 deleted=1，不物理删除
        Customer customer = customerMapper.selectById(id);
        if (customer == null) return false;
        customer.setDeleted(1);
        return customerMapper.updateById(customer) > 0;
    }

    // ---------------------- 搜索引擎（含数据权限）----------------------

    @Transactional(readOnly = true)
    public IPage<Customer> search(String keyword, String vipLevel, Integer salesPerson, boolean isAdmin, Integer page, Integer size) {
        LambdaQueryWrapper<Customer> wrapper = buildSearchWrapper(keyword, vipLevel, salesPerson, isAdmin);
        // 按VIP等级排序：VIP3 > VIP2 > VIP1 > 普通会员，同级按ID升序
        wrapper.last("ORDER BY FIELD(vip_level, 'VIP3','VIP2','VIP1','普通会员'), id ASC");
        return customerMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /** 按权限获取全部客户（用于导出），不分页 */
    @Transactional(readOnly = true)
    public List<Customer> searchAll(String keyword, String vipLevel, Integer salesPerson, boolean isAdmin) {
        LambdaQueryWrapper<Customer> wrapper = buildSearchWrapper(keyword, vipLevel, salesPerson, isAdmin);
        wrapper.last("ORDER BY FIELD(vip_level, 'VIP3','VIP2','VIP1','普通会员'), id ASC");
        return customerMapper.selectList(wrapper);
    }

    private LambdaQueryWrapper<Customer> buildSearchWrapper(String keyword, String vipLevel,
                                                             Integer salesPerson, boolean isAdmin) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Customer::getName, keyword)
                    .or().like(Customer::getContactPerson, keyword)
                    .or().like(Customer::getPhone, keyword)
                    .or().like(Customer::getEmail, keyword)
            );
        }
        if (vipLevel != null && !vipLevel.trim().isEmpty()) {
            wrapper.eq(Customer::getVipLevel, vipLevel);
        }
        // 数据权限：非管理员只能看自己的客户
        if (!isAdmin && salesPerson != null) {
            wrapper.eq(Customer::getSalesPerson, salesPerson);
        }
        // 过滤已删除记录
        wrapper.eq(Customer::getDeleted, 0);
        return wrapper;
    }

    private void validateCustomer(Customer customer) {
        ValidationUtil.requireNotBlank(customer.getName(), "客户名称");
        if (customer.getPhone() != null && !customer.getPhone().trim().isEmpty()) {
            ValidationUtil.requireValidPhone(customer.getPhone());
        }
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            ValidationUtil.requireValidEmail(customer.getEmail());
        }
    }

    // ---------------------- 省份/城市统计 ----------------------

    @Transactional(readOnly = true)
    public List<Customer> listAllForStats() {
        return customerMapper.selectList(null);
    }

    // ---------------------- Excel 导出 ----------------------

    public void exportToExcel(HttpServletResponse response, List<Customer> customers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("客户列表");
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "客户名称", "联系人", "电话", "邮箱", "地址", "VIP等级", "标签"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(ExcelUtil.createHeaderStyle(workbook));
            }
            int rowNum = 1;
            for (Customer c : customers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getId() != null ? c.getId() : 0);
                row.createCell(1).setCellValue(c.getName() != null ? c.getName() : "");
                row.createCell(2).setCellValue(c.getContactPerson() != null ? c.getContactPerson() : "");
                row.createCell(3).setCellValue(c.getPhone() != null ? c.getPhone() : "");
                row.createCell(4).setCellValue(c.getEmail() != null ? c.getEmail() : "");
                row.createCell(5).setCellValue(c.getAddress() != null ? c.getAddress() : "");
                row.createCell(6).setCellValue(c.getVipLevel() != null ? c.getVipLevel() : "");
                row.createCell(7).setCellValue(c.getTags() != null ? c.getTags() : "");
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("客户数据_" + System.currentTimeMillis(), "UTF-8")
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
                    String contactPerson = ExcelUtil.getCellValue(row.getCell(2));
                    String phone = ExcelUtil.getCellValue(row.getCell(3));
                    String email = ExcelUtil.getCellValue(row.getCell(4));
                    String address = ExcelUtil.getCellValue(row.getCell(5));
                    String vipLevel = ExcelUtil.getCellValue(row.getCell(6));
                    String tags = ExcelUtil.getCellValue(row.getCell(7));

                    // 跳过完全空行
                    if (name.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    // 去重：名称 + 电话 已存在则跳过（功能清单要求）
                    if (!phone.isEmpty()) {
                        boolean exists = customerMapper.selectList(
                                new LambdaQueryWrapper<Customer>()
                                        .eq(Customer::getName, name)
                                        .eq(Customer::getPhone, phone)
                        ).size() > 0;
                        if (exists) {
                            skipped++;
                            continue;
                        }
                    }

                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setContactPerson(contactPerson);
                    customer.setPhone(phone);
                    customer.setEmail(email);
                    customer.setAddress(address);
                    if (!vipLevel.isEmpty()) customer.setVipLevel(vipLevel);
                    if (!tags.isEmpty()) customer.setTags(tags);

                    validateCustomer(customer);
                    customerMapper.insert(customer);
                    inserted++;
                } catch (BusinessException e) {
                    errors.add("第" + (i + 1) + "行校验失败: " + e.getMessage());
                    skipped++;
                }
            }
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
