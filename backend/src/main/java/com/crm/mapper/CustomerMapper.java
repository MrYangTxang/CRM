package com.crm.mapper;

import com.crm.entity.Customer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CustomerMapper {
    @Select("SELECT * FROM customer")
    List<Customer> findAll();

    @Select("SELECT * FROM customer WHERE id = #{id}")
    Customer findById(Integer id);

    @Insert("INSERT INTO customer(name, contact_person, phone, email, address) VALUES(#{name}, #{contactPerson}, #{phone}, #{email}, #{address})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Customer customer);

    @Update("UPDATE customer SET name=#{name}, contact_person=#{contactPerson}, phone=#{phone}, email=#{email}, address=#{address}, update_time=NOW() WHERE id=#{id}")
    int update(Customer customer);

    @Delete("DELETE FROM customer WHERE id = #{id}")
    int deleteById(Integer id);
}