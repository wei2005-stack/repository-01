package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tqh.studentmanage.entity.AppUser;
import com.tqh.studentmanage.entity.Student;
import com.tqh.studentmanage.mapper.AppUserMapper;
import com.tqh.studentmanage.mapper.StudentMapper;
import com.tqh.studentmanage.security.AuthUser;
import com.tqh.studentmanage.security.MigratingPasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final AppUserMapper appUserMapper;
    private final StudentMapper studentMapper;
    private final MigratingPasswordEncoder passwordEncoder;

    public UserController(AppUserMapper appUserMapper, StudentMapper studentMapper,
                          MigratingPasswordEncoder passwordEncoder) {
        this.appUserMapper = appUserMapper;
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping({"", "/"})
    public String list(Model model) {
        List<AppUser> users = appUserMapper.selectList(
                new LambdaQueryWrapper<AppUser>().orderByAsc(AppUser::getId));
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .select(Student::getStuId, Student::getName)
                .orderByAsc(Student::getStuId));
        model.addAttribute("users", users);
        model.addAttribute("students", students);
        return "user_manage";
    }

    @PostMapping("/add")
    public String add(@RequestParam String username,
                      @RequestParam String password,
                      @RequestParam(defaultValue = "") String realName,
                      @RequestParam(defaultValue = "readonly") String role,
                      @RequestParam(defaultValue = "") String studentId) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setRole(role);
        user.setStudentId(StringUtils.hasText(studentId) ? studentId : null);
        user.setIsActive(true);
        try {
            appUserMapper.insert(user);
        } catch (Exception ignored) {
            // 用户名重复等情况静默忽略，与原系统一致
        }
        return "redirect:/users";
    }

    @PostMapping("/{userId}/toggle")
    public String toggle(@PathVariable Integer userId) {
        AppUser user = appUserMapper.selectById(userId);
        if (user != null) {
            boolean active = !Boolean.TRUE.equals(user.getIsActive());
            appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                    .eq(AppUser::getId, userId)
                    .set(AppUser::getIsActive, active));
        }
        return "redirect:/users";
    }

    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable Integer userId,
                         @AuthenticationPrincipal AuthUser currentUser) {
        if (currentUser != null && userId.equals(currentUser.getId())) {
            return "redirect:/users";
        }
        appUserMapper.deleteById(userId);
        return "redirect:/users";
    }

    @PostMapping("/{userId}/reset-password")
    public String resetPassword(@PathVariable Integer userId,
                                @RequestParam(defaultValue = "123456") String newPassword) {
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, userId)
                .set(AppUser::getPassword, passwordEncoder.encode(newPassword)));
        return "redirect:/users";
    }
}
