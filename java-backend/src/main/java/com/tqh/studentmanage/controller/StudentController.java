package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.dto.ImportResult;
import com.tqh.studentmanage.entity.Department;
import com.tqh.studentmanage.entity.RewardPunishment;
import com.tqh.studentmanage.entity.Student;
import com.tqh.studentmanage.mapper.ClassInfoMapper;
import com.tqh.studentmanage.mapper.DepartmentMapper;
import com.tqh.studentmanage.mapper.MajorMapper;
import com.tqh.studentmanage.mapper.RewardMapper;
import com.tqh.studentmanage.mapper.ScoreMapper;
import com.tqh.studentmanage.mapper.StudentMapper;
import com.tqh.studentmanage.security.AuthUser;
import com.tqh.studentmanage.service.StudentService;
import com.tqh.studentmanage.util.UrlUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentMapper studentMapper;
    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;
    private final ClassInfoMapper classInfoMapper;
    private final ScoreMapper scoreMapper;
    private final RewardMapper rewardMapper;

    public StudentController(StudentService studentService, StudentMapper studentMapper,
                             DepartmentMapper departmentMapper, MajorMapper majorMapper,
                             ClassInfoMapper classInfoMapper, ScoreMapper scoreMapper,
                             RewardMapper rewardMapper) {
        this.studentService = studentService;
        this.studentMapper = studentMapper;
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
        this.classInfoMapper = classInfoMapper;
        this.scoreMapper = scoreMapper;
        this.rewardMapper = rewardMapper;
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(defaultValue = "") String deptId,
                       @RequestParam(defaultValue = "") String majorId,
                       @RequestParam(defaultValue = "") String classId,
                       @RequestParam(defaultValue = "") String enrollYear,
                       @RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       @AuthenticationPrincipal AuthUser user,
                       Model model) {
        if (user != null && user.isReadonly()) {
            return "redirect:/statistics/my-info";
        }

        Integer year = StringUtils.hasText(enrollYear) ? Integer.valueOf(enrollYear) : null;
        long total = studentService.count(deptId, majorId, classId, year, keyword);
        int currentPage = studentService.resolvePage(page, total);

        model.addAttribute("students", studentService.page(deptId, majorId, classId, year, keyword, currentPage, total));
        model.addAttribute("departments", departmentMapper.selectList(
                new LambdaQueryWrapper<Department>().orderByAsc(Department::getDeptId)));
        model.addAttribute("majors", majorMapper.selectWithDept());
        model.addAttribute("classes", classInfoMapper.selectWithMajorAndDept());
        model.addAttribute("years", studentMapper.selectEnrollYears());
        model.addAttribute("filterDept", deptId);
        model.addAttribute("filterMajor", majorId);
        model.addAttribute("filterClass", classId);
        model.addAttribute("filterYear", enrollYear);
        model.addAttribute("filterKeyword", keyword);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", studentService.totalPages(total));
        model.addAttribute("total", total);
        model.addAttribute("queryString", UrlUtils.queryString(
                "deptId", deptId, "majorId", majorId, "classId", classId,
                "enrollYear", enrollYear, "keyword", keyword));
        return "student";
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "") String deptId,
                                         @RequestParam(defaultValue = "") String majorId,
                                         @RequestParam(defaultValue = "") String classId,
                                         @RequestParam(defaultValue = "") String enrollYear) {
        Integer year = StringUtils.hasText(enrollYear) ? Integer.valueOf(enrollYear) : null;
        String csv = studentService.exportCsv(deptId, majorId, classId, year);
        String fileName = studentService.exportFileName(deptId, majorId, classId, enrollYear);
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
        return "student_import";
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String importStudents(@RequestParam("file") MultipartFile file,
                                 @RequestParam(name = "skip_errors", defaultValue = "") String skipErrors,
                                 Model model) {
        ImportResult result = studentService.importCsv(file, "1".equals(skipErrors));
        model.addAttribute("result", result);
        return "student_import";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String addForm(Model model) {
        model.addAttribute("student", null);
        model.addAttribute("classes", classInfoMapper.selectWithMajorAndDept());
        model.addAttribute("action", "add");
        return "student_form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String add(@RequestParam String stuId,
                      @RequestParam String name,
                      @RequestParam String gender,
                      @RequestParam(defaultValue = "") String birthDate,
                      @RequestParam(defaultValue = "") String idCard,
                      @RequestParam(defaultValue = "") String phone,
                      @RequestParam(defaultValue = "") String address,
                      @RequestParam String enrollDate,
                      @RequestParam String classId) {
        Student student = new Student();
        student.setStuId(stuId);
        student.setName(name);
        student.setGender(gender);
        student.setBirthDate(parseDate(birthDate));
        student.setIdCard(emptyToNull(idCard));
        student.setPhone(emptyToNull(phone));
        student.setAddress(emptyToNull(address));
        student.setEnrollDate(parseDate(enrollDate));
        student.setClassId(classId);
        studentMapper.insert(student);
        studentService.invalidateCountCache();
        return "redirect:/students";
    }

    @GetMapping("/{stuId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String editForm(@PathVariable String stuId, Model model) {
        Student student = studentMapper.selectById(stuId);
        if (student == null) {
            return "redirect:/students";
        }
        model.addAttribute("student", student);
        model.addAttribute("classes", classInfoMapper.selectWithMajorAndDept());
        model.addAttribute("action", "edit");
        return "student_form";
    }

    @PostMapping("/{stuId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String update(@PathVariable String stuId,
                         @RequestParam String name,
                         @RequestParam String gender,
                         @RequestParam(defaultValue = "") String birthDate,
                         @RequestParam(defaultValue = "") String idCard,
                         @RequestParam(defaultValue = "") String phone,
                         @RequestParam(defaultValue = "") String address,
                         @RequestParam String enrollDate,
                         @RequestParam String classId) {
        Student student = new Student();
        student.setStuId(stuId);
        student.setName(name);
        student.setGender(gender);
        student.setBirthDate(parseDate(birthDate));
        student.setIdCard(emptyToNull(idCard));
        student.setPhone(emptyToNull(phone));
        student.setAddress(emptyToNull(address));
        student.setEnrollDate(parseDate(enrollDate));
        student.setClassId(classId);
        studentMapper.updateById(student);
        studentService.invalidateCountCache();
        return "redirect:/students";
    }

    @GetMapping("/{stuId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable String stuId) {
        try {
            studentMapper.deleteById(stuId);
        } catch (Exception ignored) {
            // 存在关联成绩/账号时无法删除
        }
        studentService.invalidateCountCache();
        return "redirect:/students";
    }

    @GetMapping("/{stuId}/detail")
    public String detail(@PathVariable String stuId, Model model) {
        Student student = studentMapper.selectStudentDetail(stuId);
        if (student == null) {
            return "redirect:/students";
        }
        model.addAttribute("student", student);
        model.addAttribute("scores", scoreMapper.selectByStudent(stuId));
        model.addAttribute("rewards", rewardMapper.selectList(
                new LambdaQueryWrapper<RewardPunishment>()
                        .eq(RewardPunishment::getStuId, stuId)
                        .orderByDesc(RewardPunishment::getRpDate)));
        return "student_detail";
    }

    private static LocalDate parseDate(String value) {
        return StringUtils.hasText(value) ? LocalDate.parse(value) : null;
    }

    private static String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
