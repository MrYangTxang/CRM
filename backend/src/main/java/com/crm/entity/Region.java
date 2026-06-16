package com.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("region")
public class Region {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer parentId;  // 0=顶级（省），下级指向父ID
    private Integer level;     // 1=省, 2=市, 3=区
}
