package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        return customerMapper.deleteById(id) > 0;
    }

    // ---------------------- 搜索引擎（含数据权限）----------------------

    @Transactional(readOnly = true)
    public IPage<Customer> search(String keyword, String vipLevel, Integer salesPerson, boolean isAdmin, Integer page, Integer size) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Customer::getName, keyword)
                    .or().like(Customer::getContactPerson, keyword)
                    .or().like(Customer::getPhone, keyword)
                    .or().like(Customer::getEmail, keyword)
            );
        }
        // VIP等级筛选
        if (vipLevel != null && !vipLevel.trim().isEmpty()) {
            wrapper.eq(Customer::getVipLevel, vipLevel);
        }
        // 数据权限：非管理员只能看自己的客户
        if (!isAdmin && salesPerson != null) {
            wrapper.eq(Customer::getSalesPerson, salesPerson);
        }
        // 按VIP等级排序：VIP3 > VIP2 > VIP1 > 普通会员，同级按ID升序
        wrapper.last("ORDER BY FIELD(vip_level, 'VIP3','VIP2','VIP1','普通会员'), id ASC");
        return customerMapper.selectPage(new Page<>(page, size), wrapper);
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

    // ---------------------- Excel 导入导出 ----------------------

    public void exportToExcel(HttpServletResponse response, List<Customer> customers) throws IOException {
        Workbook workbook = new XSSFWorkbook();
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
            row.createCell(0).setCellValue(c.getId());
            row.createCell(1).setCellValue(c.getName());
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
        String fileName = URLEncoder.encode("客户数据_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
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
            Customer customer = new Customer();
            customer.setName(ExcelUtil.getCellValue(row.getCell(1)));
            customer.setContactPerson(ExcelUtil.getCellValue(row.getCell(2)));
            customer.setPhone(ExcelUtil.getCellValue(row.getCell(3)));
            customer.setEmail(ExcelUtil.getCellValue(row.getCell(4)));
            customer.setAddress(ExcelUtil.getCellValue(row.getCell(5)));
            String vipLevel = ExcelUtil.getCellValue(row.getCell(6));
            if (!vipLevel.isEmpty()) customer.setVipLevel(vipLevel);
            String tags = ExcelUtil.getCellValue(row.getCell(7));
            if (!tags.isEmpty()) customer.setTags(tags);
            customerMapper.insert(customer);
        }
        workbook.close();
    }
}
