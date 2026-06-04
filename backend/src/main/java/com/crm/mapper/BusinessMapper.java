package com.crm.mapper;

import com.crm.entity.Business;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BusinessMapper {

    @Select("SELECT * FROM business")
    List<Business> findAll();

    @Select("SELECT * FROM business WHERE id = #{id}")
    Business findById(Integer id);

    @Select("SELECT * FROM business WHERE customer_id = #{customerId}")
    List<Business> findByCustomerId(Integer customerId);

    @Insert("INSERT INTO business(name, type, price, status, customer_id) VALUES(#{name}, #{type}, #{price}, #{status}, #{customerId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Business business);

    @Update("UPDATE business SET name=#{name}, type=#{type}, price=#{price}, status=#{status}, customer_id=#{customerId}, update_time=NOW() WHERE id=#{id}")
    int update(Business business);

    @Delete("DELETE FROM business WHERE id = #{id}")
    int deleteById(Integer id);
}