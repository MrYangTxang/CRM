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
@TableName("follow_up")
public class FollowUp {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer customerId;
    private String content;
    private Integer staffId;
    private LocalDateTime createTime;
}
