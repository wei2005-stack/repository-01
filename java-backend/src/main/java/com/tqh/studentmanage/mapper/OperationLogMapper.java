package com.tqh.studentmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tqh.studentmanage.entity.OperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OperationLogMapper extends BaseMapper<OperationLog> {

    List<OperationLog> selectFiltered(@Param("tableName") String tableName,
                                      @Param("operation") String operation,
                                      @Param("keyword") String keyword);

    List<String> selectDistinctTables();
}
