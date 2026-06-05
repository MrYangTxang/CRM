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
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String method;
    private String url;
    private String params;
    private String ip;
    private LocalDateTime createTime;
}
