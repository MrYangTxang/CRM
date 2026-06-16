package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.Business;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BusinessMapper extends BaseMapper<Business> {

    @Select("SELECT * FROM business WHERE customer_id = #{customerId}")
    List<Business> findByCustomerId(Integer customerId);

    /** 按名称+客户ID查询，用于Excel导入去重 */
    @Select("SELECT * FROM business WHERE name = #{name} AND customer_id = #{customerId} LIMIT 1")
    Business findByNameAndCustomerId(@Param("name") String name, @Param("customerId") Integer customerId);
}
