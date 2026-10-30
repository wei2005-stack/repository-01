package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.entity.RewardPunishment;
import com.tqh.studentmanage.entity.Student;
import com.tqh.studentmanage.mapper.RewardMapper;
import com.tqh.studentmanage.mapper.ScoreMapper;
import com.tqh.studentmanage.mapper.StatisticsMapper;
import com.tqh.studentmanage.mapper.StudentMapper;
import com.tqh.studentmanage.security.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    private static final int PAGE_SIZE = 15;

    private final StatisticsMapper statisticsMapper;
    private final StudentMapper studentMapper;
    private final ScoreMapper scoreMapper;
    private final RewardMapper rewardMapper;

    public StatisticsController(StatisticsMapper statisticsMapper, StudentMapper studentMapper,
                                ScoreMapper scoreMapper, RewardMapper rewardMapper) {
        this.statisticsMapper = statisticsMapper;
        this.studentMapper = studentMapper;
        this.scoreMapper = scoreMapper;
        this.rewardMapper = rewardMapper;
    }

    @GetMapping("/gender-dept")
    public String genderByDept(Model model) {
        addTable(model, "各院系男女人数统计", "调用存储过程 sp_GenderStatsByDept",
                List.of("院系", "男生人数", "女生人数", "总人数"),
                statisticsMapper.callGenderStatsByDept());
        return "statistics";
    }

    @GetMapping("/gender-major")
    public String genderByMajor(Model model) {
        addTable(model, "各专业男女人数统计", "调用存储过程 sp_GenderStatsByMajor",
                List.of("院系", "专业", "男生人数", "女生人数", "总人数"),
                statisticsMapper.callGenderStatsByMajor());
        return "statistics";
    }

    @GetMapping("/student-info")
    public String studentInfoView(@RequestParam(defaultValue = "1") int page,
                                  @AuthenticationPrincipal AuthUser user,
                                  Model model) {
        List<Map<String, Object>> data;
        long total;
        int currentPage;
        int totalPages;
        if (isStudent(user)) {
            data = statisticsMapper.selectStudentInfoByStuId(user.getStudentId());
            total = data.size();
            currentPage = 1;
            totalPages = 1;
        } else {
            total = statisticsMapper.countStudentInfoView();
            totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            currentPage = Math.max(1, Math.min(page, totalPages));
            data = statisticsMapper.selectStudentInfoView((currentPage - 1) * PAGE_SIZE, PAGE_SIZE);
        }
        addTable(model, "学生基本信息视图", "查询视图 v_StudentInfo",
                List.of("学号", "姓名", "性别", "出生日期", "联系电话", "院系", "专业", "班级", "入学时间"),
                data);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        model.addAttribute("queryString", "");
        return "statistics";
    }

    @GetMapping("/score-view")
    public String scoreView(@RequestParam(defaultValue = "1") int page,
                            @AuthenticationPrincipal AuthUser user,
                            Model model) {
        boolean isStudent = isStudent(user);
        List<Map<String, Object>> data;
        Map<String, Object> scoreDist;
        List<Map<String, Object>> courseScores;
        long total;
        int currentPage;
        int totalPages;

        if (isStudent) {
            data = statisticsMapper.selectStudentScoreByStuId(user.getStudentId());
            total = data.size();
            currentPage = 1;
            totalPages = 1;
            scoreDist = statisticsMapper.selectScoreDistributionByStuId(user.getStudentId());
            courseScores = statisticsMapper.selectStudentCourseScores(user.getStudentId());
        } else {
            total = statisticsMapper.countStudentScoreView();
            totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            currentPage = Math.max(1, Math.min(page, totalPages));
            data = statisticsMapper.selectStudentScoreView((currentPage - 1) * PAGE_SIZE, PAGE_SIZE);
            scoreDist = statisticsMapper.selectScoreDistribution();
            courseScores = statisticsMapper.selectCourseAverages();
        }

        addTable(model, "学生成绩综合视图", "查询视图 v_StudentScore",
                List.of("学号", "姓名", "院系", "专业", "班级", "课程", "学分", "成绩", "学期", "考试类型", "等级"),
                data);
        model.addAttribute("scoreDist", scoreDist);
        model.addAttribute("courseScores", courseScores);
        model.addAttribute("distValues", List.of(
                zeroIfNull(scoreDist, "优秀"),
                zeroIfNull(scoreDist, "良好"),
                zeroIfNull(scoreDist, "中等"),
                zeroIfNull(scoreDist, "及格"),
                zeroIfNull(scoreDist, "不及格")));
        model.addAttribute("courseLabels", courseScores.stream()
                .map(row -> String.valueOf(row.get("course_name"))).toList());
        model.addAttribute("courseValues", courseScores.stream()
                .map(row -> isStudent ? row.get("score") : row.get("avg_score")).toList());
        model.addAttribute("isStudent", isStudent);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        model.addAttribute("queryString", "");
        return "score_chart";
    }

    @GetMapping("/failed")
    public String failedStudents(@AuthenticationPrincipal AuthUser user, Model model) {
        List<Map<String, Object>> data = isStudent(user)
                ? statisticsMapper.selectFailedStudentsByStuId(user.getStudentId())
                : statisticsMapper.selectFailedStudents();
        addTable(model, "不及格学生视图", "查询视图 v_FailedStudents",
                List.of("学号", "姓名", "院系", "专业", "班级", "课程", "成绩", "学期"), data);
        return "statistics";
    }

    @GetMapping("/class-stats")
    public String classStats(@AuthenticationPrincipal AuthUser user, Model model) {
        List<Map<String, Object>> data = isStudent(user)
                ? statisticsMapper.selectClassStatisticsByStuId(user.getStudentId())
                : statisticsMapper.selectClassStatistics();
        addTable(model, "班级学生统计视图", "查询视图 v_ClassStatistics",
                List.of("班级编号", "班级名称", "专业", "院系", "入学年份", "系统人数", "实际人数"), data);
        return "statistics";
    }

    @GetMapping("/my-info")
    public String myInfo(@AuthenticationPrincipal AuthUser user, Model model) {
        if (!isStudent(user)) {
            return "redirect:/";
        }
        Student student = studentMapper.selectStudentDetail(user.getStudentId());
        if (student == null) {
            return "redirect:/";
        }
        model.addAttribute("student", student);
        model.addAttribute("scores", scoreMapper.selectByStudent(user.getStudentId()));
        model.addAttribute("rewards", rewardMapper.selectList(
                new LambdaQueryWrapper<RewardPunishment>()
                        .eq(RewardPunishment::getStuId, user.getStudentId())
                        .orderByDesc(RewardPunishment::getRpDate)));
        return "my_info";
    }

    private static void addTable(Model model, String title, String description,
                                 List<String> headers, List<Map<String, Object>> data) {
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("headers", headers);
        model.addAttribute("columns", headers);
        model.addAttribute("data", data);
    }

    private static boolean isStudent(AuthUser user) {
        return user != null && user.isReadonly() && StringUtils.hasText(user.getStudentId());
    }

    private static Object zeroIfNull(Map<String, Object> row, String key) {
        return row == null || row.get(key) == null ? 0 : row.get(key);
    }
}
