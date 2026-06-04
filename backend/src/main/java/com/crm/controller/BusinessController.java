package com.crm.controller;

import com.crm.entity.Business;
import com.crm.service.BusinessService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @GetMapping("/list")
    public List<Business> list() {
        return businessService.listAll();
    }

    @GetMapping("/{id}")
    public Business get(@PathVariable Integer id) {
        return businessService.getById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Business> listByCustomer(@PathVariable Integer customerId) {
        return businessService.listByCustomerId(customerId);
    }

    @PostMapping("/add")
    public String add(@RequestBody Business business) {
        return businessService.add(business) ? "添加成功" : "添加失败";
    }

    @PutMapping("/update")
    public String update(@RequestBody Business business) {
        return businessService.update(business) ? "更新成功" : "更新失败";
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        return businessService.delete(id) ? "删除成功" : "删除失败";
    }
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        businessService.exportToExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        businessService.importFromExcel(file);
        return "导入成功";
    }
}