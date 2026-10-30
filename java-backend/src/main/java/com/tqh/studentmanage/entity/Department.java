package com.tqh.studentmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("Department")
public class Department {

    @TableId(type = IdType.INPUT)
    private String deptId;
    private String deptName;
    private String phone;
    private String address;
}
