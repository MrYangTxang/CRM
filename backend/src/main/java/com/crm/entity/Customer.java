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
@TableName("customer")
public class Customer {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String vipLevel;
    private String tags;
    private Integer salesPerson;
    private Integer deleted;       // 0=正常, 1=已删除（软删除）
    private String status;         // active / churned
    private String churnReason;    // 流失原因
    private LocalDateTime churnTime;    // 流失时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
