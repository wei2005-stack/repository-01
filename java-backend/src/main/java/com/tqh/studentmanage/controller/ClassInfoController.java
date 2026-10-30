package com.tqh.studentmanage.controller;

import com.tqh.studentmanage.entity.ClassInfo;
import com.tqh.studentmanage.mapper.ClassInfoMapper;
import com.tqh.studentmanage.mapper.MajorMapper;
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

@Controller
@RequestMapping("/classes")
public class ClassInfoController {

    private final ClassInfoMapper classInfoMapper;
    private final MajorMapper majorMapper;

    public ClassInfoController(ClassInfoMapper classInfoMapper, MajorMapper majorMapper) {
        this.classInfoMapper = classInfoMapper;
        this.majorMapper = majorMapper;
    }

    @GetMapping({"", "/"})
    public String list(@AuthenticationPrincipal AuthUser user, Model model) {
        if (user != null && user.isReadonly()) {
            return "redirect:/statistics/my-info";
        }
        model.addAttribute("classes", classInfoMapper.selectWithMajorAndDept());
        return "class_info";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String addForm(Model model) {
        model.addAttribute("cls", null);
        model.addAttribute("majors", majorMapper.selectWithDept());
        model.addAttribute("action", "add");
        return "class_form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String add(@RequestParam String classId,
                      @RequestParam String className,
                      @RequestParam String majorId,
                      @RequestParam Integer enrollYear) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassId(classId);
        classInfo.setClassName(className);
        classInfo.setMajorId(majorId);
        classInfo.setEnrollYear(enrollYear);
        classInfoMapper.insert(classInfo);
        return "redirect:/classes";
    }

    @GetMapping("/{classId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String editForm(@PathVariable String classId, Model model) {
        ClassInfo classInfo = classInfoMapper.selectById(classId);
        if (classInfo == null) {
            return "redirect:/classes";
        }
        model.addAttribute("cls", classInfo);
        model.addAttribute("majors", majorMapper.selectWithDept());
        model.addAttribute("action", "edit");
        return "class_form";
    }

    @PostMapping("/{classId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String update(@PathVariable String classId,
                         @RequestParam String className,
                         @RequestParam String majorId,
                         @RequestParam Integer enrollYear) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassId(classId);
        classInfo.setClassName(className);
        classInfo.setMajorId(majorId);
        classInfo.setEnrollYear(enrollYear);
        classInfoMapper.updateById(classInfo);
        return "redirect:/classes";
    }

    @GetMapping("/{classId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable String classId) {
        try {
            classInfoMapper.deleteById(classId);
        } catch (Exception ignored) {
            // 存在关联学生时无法删除
        }
        return "redirect:/classes";
    }
}
