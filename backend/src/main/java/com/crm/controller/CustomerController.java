package com.crm.controller;

import com.crm.entity.Customer;
import com.crm.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/list")
    public List<Customer> list() {
        return customerService.listAll();
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Integer id) {
        return customerService.getById(id);
    }

    @PostMapping("/add")
    public String add(@RequestBody Customer customer) {
        return customerService.add(customer) ? "添加成功" : "添加失败";
    }

    @PutMapping("/update")
    public String update(@RequestBody Customer customer) {
        return customerService.update(customer) ? "更新成功" : "更新失败";
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        return customerService.delete(id) ? "删除成功" : "删除失败";
    }
}