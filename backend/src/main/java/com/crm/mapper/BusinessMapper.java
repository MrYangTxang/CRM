package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.Business;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BusinessMapper extends BaseMapper<Business> {

    @Select("SELECT * FROM business WHERE customer_id = #{customerId}")
    List<Business> findByCustomerId(Integer customerId);
}
