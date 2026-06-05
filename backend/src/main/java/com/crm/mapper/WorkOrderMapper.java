package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    @Select("SELECT * FROM work_order WHERE region LIKE CONCAT('%', #{region}, '%')")
    List<WorkOrder> findByRegion(String region);

    @Update("UPDATE work_order SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("status") String status, @Param("id") Integer id);
}
