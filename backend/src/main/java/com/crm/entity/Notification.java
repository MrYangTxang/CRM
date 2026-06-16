package com.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("recipient_id")
    private Integer userId;
    private String type;       // customer_assign / work_order / system
    private String title;
    private String content;
    private Integer isRead;    // 0=未读, 1=已读
    private LocalDateTime createTime;
}
