package com.tqh.studentmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tqh.studentmanage.entity.ClassInfo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClassInfoMapper extends BaseMapper<ClassInfo> {

    @Select("SELECT c.*, m.major_name, d.dept_name FROM Class c "
            + "JOIN Major m ON c.major_id = m.major_id "
            + "JOIN Department d ON m.dept_id = d.dept_id ORDER BY c.class_id")
    List<ClassInfo> selectWithMajorAndDept();
}
