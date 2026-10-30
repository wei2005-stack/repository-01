package com.tqh.studentmanage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tqh.studentmanage.entity.Score;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ScoreMapper extends BaseMapper<Score> {

    List<Score> selectScorePage(@Param("stuId") String stuId,
                                @Param("stuName") String stuName,
                                @Param("courseName") String courseName,
                                @Param("semester") String semester,
                                @Param("offset") int offset,
                                @Param("size") int size);

    long countScores(@Param("stuId") String stuId,
                     @Param("stuName") String stuName,
                     @Param("courseName") String courseName,
                     @Param("semester") String semester);

    Score selectScoreOne(@Param("stuId") String stuId,
                         @Param("courseId") String courseId,
                         @Param("semester") String semester,
                         @Param("examType") String examType);

    List<Score> selectByStudent(@Param("stuId") String stuId);

    List<Score> selectForExport(@Param("stuId") String stuId,
                                @Param("stuName") String stuName,
                                @Param("courseName") String courseName,
                                @Param("semester") String semester,
                                @Param("deptId") String deptId,
                                @Param("majorId") String majorId,
                                @Param("classId") String classId);

    int updateScore(@Param("stuId") String stuId,
                    @Param("courseId") String courseId,
                    @Param("semester") String semester,
                    @Param("examType") String examType,
                    @Param("score") BigDecimal score);

    int deleteScore(@Param("stuId") String stuId,
                    @Param("courseId") String courseId,
                    @Param("semester") String semester,
                    @Param("examType") String examType);
}
