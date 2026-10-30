package com.tqh.studentmanage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("OperationLog")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Integer logId;
    private String username;
    private String operation;
    private String tableName;
    private String recordId;
    private String oldValue;
    private String newValue;
    private String opTime;
}
