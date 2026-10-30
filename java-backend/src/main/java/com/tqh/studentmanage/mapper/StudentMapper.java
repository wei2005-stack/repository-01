package com.tqh.studentmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tqh.studentmanage.entity.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentMapper extends BaseMapper<Student> {

    List<Student> selectStudentPage(@Param("deptId") String deptId,
                                    @Param("majorId") String majorId,
                                    @Param("classId") String classId,
                                    @Param("enrollYear") Integer enrollYear,
                                    @Param("keyword") String keyword,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    long countStudents(@Param("deptId") String deptId,
                       @Param("majorId") String majorId,
                       @Param("classId") String classId,
                       @Param("enrollYear") Integer enrollYear,
                       @Param("keyword") String keyword);

    Student selectStudentDetail(@Param("stuId") String stuId);

    List<Student> selectForExport(@Param("deptId") String deptId,
                                  @Param("majorId") String majorId,
                                  @Param("classId") String classId,
                                  @Param("enrollYear") Integer enrollYear,
                                  @Param("keyword") String keyword);

    List<Student> selectRecentStudents();

    List<Integer> selectEnrollYears();
}
