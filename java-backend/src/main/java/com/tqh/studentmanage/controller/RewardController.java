package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.entity.RewardPunishment;
import com.tqh.studentmanage.entity.Student;
import com.tqh.studentmanage.mapper.RewardMapper;
import com.tqh.studentmanage.mapper.StudentMapper;
import com.tqh.studentmanage.security.AuthUser;
import com.tqh.studentmanage.util.UrlUtils;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/rewards")
public class RewardController {

    private static final int PAGE_SIZE = 10;

    private final RewardMapper rewardMapper;
    private final StudentMapper studentMapper;

    public RewardController(RewardMapper rewardMapper, StudentMapper studentMapper) {
        this.rewardMapper = rewardMapper;
        this.studentMapper = studentMapper;
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       @AuthenticationPrincipal AuthUser user,
                       Model model) {
        if (user != null && user.isReadonly()) {
            return "redirect:/statistics/my-info";
        }
        String like = StringUtils.hasText(keyword) ? "%" + keyword + "%" : keyword;
        long total = rewardMapper.countRewards(like);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int currentPage = Math.max(1, Math.min(page, totalPages));

        model.addAttribute("rewards", rewardMapper.selectRewardPage(like,
                (currentPage - 1) * PAGE_SIZE, PAGE_SIZE));
        model.addAttribute("filterKeyword", keyword);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        model.addAttribute("queryString", UrlUtils.queryString("keyword", keyword));
        return "reward";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String addForm(Model model) {
        model.addAttribute("rp", null);
        model.addAttribute("students", listStudents());
        model.addAttribute("action", "add");
        return "reward_form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String add(@RequestParam String stuId,
                      @RequestParam String rpType,
                      @RequestParam(defaultValue = "") String description,
                      @RequestParam String rpDate,
                      @RequestParam(defaultValue = "") String level) {
        RewardPunishment reward = new RewardPunishment();
        reward.setStuId(stuId);
        reward.setRpType(rpType);
        reward.setDescription(description);
        reward.setRpDate(LocalDate.parse(rpDate));
        reward.setLevel(level);
        rewardMapper.insert(reward);
        return "redirect:/rewards";
    }

    @GetMapping("/{rpId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String editForm(@PathVariable Integer rpId, Model model) {
        RewardPunishment reward = rewardMapper.selectRewardDetail(rpId);
        if (reward == null) {
            return "redirect:/rewards";
        }
        model.addAttribute("rp", reward);
        model.addAttribute("students", listStudents());
        model.addAttribute("action", "edit");
        return "reward_form";
    }

    @PostMapping("/{rpId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACADEMIC')")
    public String update(@PathVariable Integer rpId,
                         @RequestParam String stuId,
                         @RequestParam String rpType,
                         @RequestParam(defaultValue = "") String description,
                         @RequestParam String rpDate,
                         @RequestParam(defaultValue = "") String level) {
        RewardPunishment reward = new RewardPunishment();
        reward.setRpId(rpId);
        reward.setStuId(stuId);
        reward.setRpType(rpType);
        reward.setDescription(description);
        reward.setRpDate(LocalDate.parse(rpDate));
        reward.setLevel(level);
        rewardMapper.updateById(reward);
        return "redirect:/rewards";
    }

    @GetMapping("/{rpId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Integer rpId) {
        rewardMapper.deleteById(rpId);
        return "redirect:/rewards";
    }

    private List<Student> listStudents() {
        return studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .select(Student::getStuId, Student::getName)
                .orderByAsc(Student::getStuId));
    }
}
