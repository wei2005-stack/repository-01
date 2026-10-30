package com.tqh.studentmanage.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface StatisticsMapper {

    List<Map<String, Object>> callGenderStatsByDept();

    List<Map<String, Object>> callGenderStatsByMajor();

    List<Map<String, Object>> callClassScoreStats(@Param("classId") String classId);

    long countStudentInfoView();

    List<Map<String, Object>> selectStudentInfoView(@Param("offset") int offset, @Param("size") int size);

    List<Map<String, Object>> selectStudentInfoByStuId(@Param("stuId") String stuId);

    long countStudentScoreView();

    List<Map<String, Object>> selectStudentScoreView(@Param("offset") int offset, @Param("size") int size);

    List<Map<String, Object>> selectStudentScoreByStuId(@Param("stuId") String stuId);

    List<Map<String, Object>> selectFailedStudents();

    List<Map<String, Object>> selectFailedStudentsByStuId(@Param("stuId") String stuId);

    List<Map<String, Object>> selectClassStatistics();

    List<Map<String, Object>> selectClassStatisticsByStuId(@Param("stuId") String stuId);

    Map<String, Object> selectScoreDistribution();

    Map<String, Object> selectScoreDistributionByStuId(@Param("stuId") String stuId);

    List<Map<String, Object>> selectCourseAverages();

    List<Map<String, Object>> selectStudentCourseScores(@Param("stuId") String stuId);

    long countDepartments();

    long countMajors();

    long countClasses();

    long countStudents();

    long countCourses();

    long countScores();

    List<Map<String, Object>> selectDeptMajorCounts();

    List<Map<String, Object>> selectDeptStudentCounts();

    List<Map<String, Object>> selectCourseTypeCounts();
}
