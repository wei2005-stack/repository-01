package com.tqh.studentmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("Score")
public class Score {

    // 复合主键，仅作为 MyBatis-Plus 的主键标记；按复合键的增删改使用自定义 SQL
    @TableId(type = IdType.INPUT)
    private String stuId;
    private String courseId;
    private BigDecimal score;
    private String semester;
    private String examType;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private BigDecimal credit;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String majorName;

    @TableField(exist = false)
    private String deptName;
}
