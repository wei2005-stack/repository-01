package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.dto.ImportResult;
import com.tqh.studentmanage.entity.Course;
import com.tqh.studentmanage.entity.Score;
import com.tqh.studentmanage.entity.Student;
import com.tqh.studentmanage.mapper.CourseMapper;
import com.tqh.studentmanage.mapper.ScoreMapper;
import com.tqh.studentmanage.mapper.StudentMapper;
import com.tqh.studentmanage.security.AuthUser;
import com.tqh.studentmanage.service.ScoreService;
import com.tqh.studentmanage.util.UrlUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/scores")
public class ScoreController {

    private final ScoreService scoreService;
    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

    public ScoreController(ScoreService scoreService, ScoreMapper scoreMapper,
                           StudentMapper studentMapper, CourseMapper courseMapper) {
        this.scoreService = scoreService;
        this.scoreMapper = scoreMapper;
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(defaultValue = "") String stuId,
                       @RequestParam(defaultValue = "") String stuName,
                       @RequestParam(defaultValue = "") String courseName,
                       @RequestParam(defaultValue = "") String semester,
                       @RequestParam(defaultValue = "1") int page,
                       @AuthenticationPrincipal AuthUser user,
                       Model model) {
        if (user != null && user.isReadonly()) {
            return "redirect:/statistics/my-info";
        }
        long total = scoreService.count(stuId, stuName, courseName, semester);
        int currentPage = scoreService.resolvePage(page, total);

        model.addAttribute("scores", scoreService.page(stuId, stuName, courseName, semester, currentPage, total));
        model.addAttribute("filterStu", stuId);
        model.addAttribute("filterStuName", stuName);
        model.addAttribute("filterCourseName", courseName);
        model.addAttribute("filterSemester", semester);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", scoreService.totalPages(total));
        model.addAttribute("total", total);
        model.addAttribute("queryString", UrlUtils.queryString(
                "stuId", stuId, "stuName", stuName, "courseName", courseName, "semester", semester));
        return "score";
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "") String stuId,
                                         @RequestParam(defaultValue = "") String stuName,
                                         @RequestParam(defaultValue = "") String courseName,
                                         @RequestParam(defaultValue = "") String semester,
                                         @RequestParam(defaultValue = "") String deptId,
                                         @RequestParam(defaultValue = "") String majorId,
                                         @RequestParam(defaultValue = "") String classId) {
        String csv = scoreService.exportCsv(stuId, stuName, courseName, semester, deptId, majorId, classId);
        String fileName = scoreService.exportFileName(stuId, stuName, courseName, semester, deptId, majorId, classId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String importForm(Model model) {
        model.addAttribute("result", null);
        return "score_import";
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String importScores(@RequestParam("file") MultipartFile file,
                               @RequestParam(name = "skip_errors", defaultValue = "") String skipErrors,
                               Model model) {
        ImportResult result = scoreService.importCsv(file, "1".equals(skipErrors));
        model.addAttribute("result", result);
        return "score_import";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String addForm(Model model) {
        model.addAttribute("score", null);
        model.addAttribute("students", listStudents());
        model.addAttribute("courses", listCourses());
        model.addAttribute("action", "add");
        return "score_form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String add(@RequestParam String stuId,
                      @RequestParam String courseId,
                      @RequestParam BigDecimal score,
                      @RequestParam String semester,
                      @RequestParam(defaultValue = "期末") String examType) {
        try {
            Score entity = new Score();
            entity.setStuId(stuId);
            entity.setCourseId(courseId);
            entity.setScore(score);
            entity.setSemester(semester);
            entity.setExamType(examType);
            scoreMapper.insert(entity);
        } catch (Exception ignored) {
            // 重复成绩记录直接忽略，与原系统一致
        }
        return "redirect:/scores";
    }

    @GetMapping("/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String editForm(@RequestParam String stuId,
                           @RequestParam String courseId,
                           @RequestParam String semester,
                           @RequestParam String examType,
                           Model model) {
        Score score = scoreMapper.selectScoreOne(stuId, courseId, semester, examType);
        if (score == null) {
            return "redirect:/scores";
        }
        model.addAttribute("score", score);
        model.addAttribute("students", listStudents());
        model.addAttribute("courses", listCourses());
        model.addAttribute("action", "edit");
        return "score_form";
    }

    @PostMapping("/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String update(@RequestParam String stuId,
                         @RequestParam String courseId,
                         @RequestParam String semester,
                         @RequestParam String examType,
                         @RequestParam BigDecimal score) {
        scoreMapper.updateScore(stuId, courseId, semester, examType, score);
        return "redirect:/scores";
    }

    @GetMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@RequestParam String stuId,
                         @RequestParam String courseId,
                         @RequestParam String semester,
                         @RequestParam String examType) {
        scoreMapper.deleteScore(stuId, courseId, semester, examType);
        return "redirect:/scores";
    }

    private java.util.List<Student> listStudents() {
        return studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .select(Student::getStuId, Student::getName)
                .orderByAsc(Student::getStuId));
    }

    private java.util.List<Course> listCourses() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .select(Course::getCourseId, Course::getCourseName)
                .orderByAsc(Course::getCourseId));
    }
}
