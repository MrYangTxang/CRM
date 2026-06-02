package com.crm.service;

import com.crm.entity.Customer;
import com.crm.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<Customer> listAll() {
        return customerMapper.findAll();
    }

    @Transactional(readOnly = true)
    public Customer getById(Integer id) {
        return customerMapper.findById(id);
    }

    @Transactional
    public boolean add(Customer customer) {
        return customerMapper.insert(customer) > 0;
    }

    @Transactional
    public boolean update(Customer customer) {
        return customerMapper.update(customer) > 0;
    }

    @Transactional
    public boolean delete(Integer id) {
        return customerMapper.deleteById(id) > 0;
    }
}