package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.entity.Department;
import com.tqh.studentmanage.entity.Major;
import com.tqh.studentmanage.mapper.DepartmentMapper;
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
@RequestMapping("/majors")
public class MajorController {

    private final MajorMapper majorMapper;
    private final DepartmentMapper departmentMapper;

    public MajorController(MajorMapper majorMapper, DepartmentMapper departmentMapper) {
        this.majorMapper = majorMapper;
        this.departmentMapper = departmentMapper;
    }

    @GetMapping({"", "/"})
    public String list(@AuthenticationPrincipal AuthUser user, Model model) {
        if (user != null && user.isReadonly()) {
            return "redirect:/statistics/my-info";
        }
        model.addAttribute("majors", majorMapper.selectWithDept());
        return "major";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String addForm(Model model) {
        model.addAttribute("major", null);
        model.addAttribute("departments", listDepartments());
        model.addAttribute("action", "add");
        return "major_form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String add(@RequestParam String majorId,
                      @RequestParam String majorName,
                      @RequestParam String deptId) {
        Major major = new Major();
        major.setMajorId(majorId);
        major.setMajorName(majorName);
        major.setDeptId(deptId);
        majorMapper.insert(major);
        return "redirect:/majors";
    }

    @GetMapping("/{majorId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String editForm(@PathVariable String majorId, Model model) {
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            return "redirect:/majors";
        }
        model.addAttribute("major", major);
        model.addAttribute("departments", listDepartments());
        model.addAttribute("action", "edit");
        return "major_form";
    }

    @PostMapping("/{majorId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String update(@PathVariable String majorId,
                         @RequestParam String majorName,
                         @RequestParam String deptId) {
        Major major = new Major();
        major.setMajorId(majorId);
        major.setMajorName(majorName);
        major.setDeptId(deptId);
        majorMapper.updateById(major);
        return "redirect:/majors";
    }

    @GetMapping("/{majorId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable String majorId) {
        try {
            majorMapper.deleteById(majorId);
        } catch (Exception ignored) {
            // 存在关联班级时无法删除
        }
        return "redirect:/majors";
    }

    private java.util.List<Department> listDepartments() {
        return departmentMapper.selectList(
                new LambdaQueryWrapper<Department>().orderByAsc(Department::getDeptId));
    }
}
