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
@TableName("todo")
public class Todo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("staff_id")
    private Integer userId;
    private String title;
    private String content;
    private String priority;   // 高/中/低
    private String status;     // pending / done
    @TableField("due_time")
    private LocalDateTime deadline;
    private LocalDateTime createTime;
}
