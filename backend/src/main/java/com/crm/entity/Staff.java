package com.crm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("staff")
public class Staff {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String username;

    @JsonIgnore
    private String password;

    private String role;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
}
