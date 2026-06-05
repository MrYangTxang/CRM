package com.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("work_order")
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderNo;
    private Integer customerId;
    private Integer businessId;
    private String region;
    private String status;
    private String priority;
    private Integer ownerId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
