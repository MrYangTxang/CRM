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
@TableName("export_history")
public class ExportHistory {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String tableName;
    private String fileName;
    private Integer recordCount;
    private String username;
    private String filePath;
    private LocalDateTime createTime;
}
