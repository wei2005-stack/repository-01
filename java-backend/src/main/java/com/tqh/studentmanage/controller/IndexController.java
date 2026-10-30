package com.tqh.studentmanage.controller;

import com.tqh.studentmanage.mapper.StatisticsMapper;
import com.tqh.studentmanage.mapper.StudentMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    private final StatisticsMapper statisticsMapper;
    private final StudentMapper studentMapper;

    public IndexController(StatisticsMapper statisticsMapper, StudentMapper studentMapper) {
        this.statisticsMapper = statisticsMapper;
        this.studentMapper = studentMapper;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("deptCount", statisticsMapper.countDepartments());
        model.addAttribute("majorCount", statisticsMapper.countMajors());
        model.addAttribute("classCount", statisticsMapper.countClasses());
        model.addAttribute("studentCount", statisticsMapper.countStudents());
        model.addAttribute("courseCount", statisticsMapper.countCourses());
        model.addAttribute("scoreCount", statisticsMapper.countScores());
        model.addAttribute("recentStudents", studentMapper.selectRecentStudents());

        List<Map<String, Object>> deptMajors = statisticsMapper.selectDeptMajorCounts();
        List<Map<String, Object>> deptStudents = statisticsMapper.selectDeptStudentCounts();
        List<Map<String, Object>> courseTypes = statisticsMapper.selectCourseTypeCounts();
        model.addAttribute("deptMajors", deptMajors);
        model.addAttribute("deptStudents", deptStudents);
        model.addAttribute("courseTypes", courseTypes);

        model.addAttribute("deptMajorLabels", deptMajors.stream().map(m -> String.valueOf(m.get("deptName"))).toList());
        model.addAttribute("deptMajorCounts", deptMajors.stream().map(m -> m.get("cnt")).toList());
        model.addAttribute("deptStudentLabels", deptStudents.stream().map(m -> String.valueOf(m.get("deptName"))).toList());
        model.addAttribute("deptStudentCounts", deptStudents.stream().map(m -> m.get("cnt")).toList());
        model.addAttribute("courseTypeLabels", courseTypes.stream().map(m -> String.valueOf(m.get("courseType"))).toList());
        model.addAttribute("courseTypeCounts", courseTypes.stream().map(m -> m.get("cnt")).toList());
        return "index";
    }
}
