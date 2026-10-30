package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.entity.Course;
import com.tqh.studentmanage.mapper.CourseMapper;
import com.tqh.studentmanage.security.AuthUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseMapper courseMapper;

    public CourseController(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @GetMapping({"", "/"})
    public String list(@AuthenticationPrincipal AuthUser user, Model model) {
        if (user != null && user.isReadonly()) {
            return "redirect:/statistics/my-info";
        }
        model.addAttribute("courses", courseMapper.selectList(
                new LambdaQueryWrapper<Course>().orderByAsc(Course::getCourseId)));
        return "course";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String addForm(Model model) {
        model.addAttribute("course", null);
        model.addAttribute("action", "add");
        return "course_form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String add(@RequestParam String courseId,
                      @RequestParam String courseName,
                      @RequestParam BigDecimal credit,
                      @RequestParam Integer hours,
                      @RequestParam String courseType) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName(courseName);
        course.setCredit(credit);
        course.setHours(hours);
        course.setCourseType(courseType);
        courseMapper.insert(course);
        return "redirect:/courses";
    }

    @GetMapping("/{courseId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String editForm(@PathVariable String courseId, Model model) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            return "redirect:/courses";
        }
        model.addAttribute("course", course);
        model.addAttribute("action", "edit");
        return "course_form";
    }

    @PostMapping("/{courseId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String update(@PathVariable String courseId,
                         @RequestParam String courseName,
                         @RequestParam BigDecimal credit,
                         @RequestParam Integer hours,
                         @RequestParam String courseType) {
        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName(courseName);
        course.setCredit(credit);
        course.setHours(hours);
        course.setCourseType(courseType);
        courseMapper.updateById(course);
        return "redirect:/courses";
    }

    @GetMapping("/{courseId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable String courseId) {
        try {
            courseMapper.deleteById(courseId);
        } catch (Exception ignored) {
            // 存在关联成绩时无法删除
        }
        return "redirect:/courses";
    }
}
