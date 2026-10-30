package com.tqh.studentmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("Course")
public class Course {

    @TableId(type = IdType.INPUT)
    private String courseId;
    private String courseName;
    private BigDecimal credit;
    private Integer hours;
    private String courseType;
}
