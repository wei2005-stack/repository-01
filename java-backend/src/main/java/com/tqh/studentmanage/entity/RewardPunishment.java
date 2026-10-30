package com.tqh.studentmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("RewardPunishment")
public class RewardPunishment {

    @TableId(type = IdType.AUTO)
    private Integer rpId;
    private String stuId;
    private String rpType;
    private String description;
    private LocalDate rpDate;
    private String level;

    @TableField(exist = false)
    private String studentName;
}
