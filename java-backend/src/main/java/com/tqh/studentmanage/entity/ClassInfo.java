package com.tqh.studentmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("Class")
public class ClassInfo {

    @TableId(type = IdType.INPUT)
    private String classId;
    private String className;
    private String majorId;
    private Integer enrollYear;
    private Integer studentCount;

    @TableField(exist = false)
    private String majorName;

    @TableField(exist = false)
    private String deptName;
}
