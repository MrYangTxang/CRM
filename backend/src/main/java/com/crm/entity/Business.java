package com.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("business")
public class Business {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String type;
    private BigDecimal price;
    private String status;
    private Integer customerId;
    private Integer ownerId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
