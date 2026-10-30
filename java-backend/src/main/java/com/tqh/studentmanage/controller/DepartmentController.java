package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.entity.Department;
import com.tqh.studentmanage.mapper.DepartmentMapper;
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
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentMapper departmentMapper;

    public DepartmentController(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @GetMapping({"", "/"})
    public String list(@AuthenticationPrincipal AuthUser user, Model model) {
        if (user != null && user.isReadonly()) {
            return "redirect:/statistics/my-info";
        }
        model.addAttribute("departments", departmentMapper.selectList(
                new LambdaQueryWrapper<Department>().orderByAsc(Department::getDeptId)));
        return "department";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String addForm(Model model) {
        model.addAttribute("dept", null);
        model.addAttribute("action", "add");
        return "department_form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String add(@RequestParam String deptId,
                      @RequestParam String deptName,
                      @RequestParam(defaultValue = "") String phone,
                      @RequestParam(defaultValue = "") String address) {
        Department department = new Department();
        department.setDeptId(deptId);
        department.setDeptName(deptName);
        department.setPhone(phone);
        department.setAddress(address);
        departmentMapper.insert(department);
        return "redirect:/departments";
    }

    @GetMapping("/{deptId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String editForm(@PathVariable String deptId, Model model) {
        Department department = departmentMapper.selectById(deptId);
        if (department == null) {
            return "redirect:/departments";
        }
        model.addAttribute("dept", department);
        model.addAttribute("action", "edit");
        return "department_form";
    }

    @PostMapping("/{deptId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String update(@PathVariable String deptId,
                         @RequestParam String deptName,
                         @RequestParam(defaultValue = "") String phone,
                         @RequestParam(defaultValue = "") String address) {
        Department department = new Department();
        department.setDeptId(deptId);
        department.setDeptName(deptName);
        department.setPhone(phone);
        department.setAddress(address);
        departmentMapper.updateById(department);
        return "redirect:/departments";
    }

    @GetMapping("/{deptId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable String deptId) {
        try {
            departmentMapper.deleteById(deptId);
        } catch (Exception ignored) {
            // 存在关联专业时无法删除，与原系统一致：静默返回列表
        }
        return "redirect:/departments";
    }
}
