package com.crm.mapper;

import com.crm.entity.WorkOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WorkOrderMapper {

    @Select("SELECT * FROM work_order")
    List<WorkOrder> findAll();

    @Select("SELECT * FROM work_order WHERE id = #{id}")
    WorkOrder findById(Integer id);

    @Select("SELECT * FROM work_order WHERE region = #{region}")
    List<WorkOrder> findByRegion(String region);

    @Insert("INSERT INTO work_order(order_no, customer_id, business_id, region, status) VALUES(#{orderNo}, #{customerId}, #{businessId}, #{region}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WorkOrder workOrder);

    @Update("UPDATE work_order SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("status") String status, @Param("id") Integer id);

    @Delete("DELETE FROM work_order WHERE id = #{id}")
    int deleteById(Integer id);
}