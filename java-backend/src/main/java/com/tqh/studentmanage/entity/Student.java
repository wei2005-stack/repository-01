package com.tqh.studentmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("Student")
public class Student {

    @TableId(type = IdType.INPUT)
    private String stuId;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String idCard;
    private String phone;
    private String address;
    private LocalDate enrollDate;
    private String classId;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String majorId;

    @TableField(exist = false)
    private String majorName;

    @TableField(exist = false)
    private String deptId;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private Integer enrollYear;
}
