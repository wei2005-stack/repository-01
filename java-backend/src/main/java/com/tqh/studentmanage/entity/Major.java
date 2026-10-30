package com.tqh.studentmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("Major")
public class Major {

    @TableId(type = IdType.INPUT)
    private String majorId;
    private String majorName;
    private String deptId;

    @TableField(exist = false)
    private String deptName;
}
