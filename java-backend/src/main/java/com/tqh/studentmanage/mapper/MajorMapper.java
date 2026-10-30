package com.tqh.studentmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tqh.studentmanage.entity.Major;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MajorMapper extends BaseMapper<Major> {

    @Select("SELECT m.*, d.dept_name FROM Major m "
            + "JOIN Department d ON m.dept_id = d.dept_id ORDER BY m.major_id")
    List<Major> selectWithDept();
}
