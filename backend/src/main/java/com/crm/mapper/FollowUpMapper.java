package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.FollowUp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowUpMapper extends BaseMapper<FollowUp> {

    @Select("SELECT * FROM follow_up WHERE customer_id = #{customerId} ORDER BY create_time DESC")
    List<FollowUp> findByCustomerId(Integer customerId);
}
