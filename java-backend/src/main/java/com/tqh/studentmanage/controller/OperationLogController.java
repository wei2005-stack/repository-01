package com.tqh.studentmanage.controller;

import com.tqh.studentmanage.entity.OperationLog;
import com.tqh.studentmanage.mapper.OperationLogMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/logs")
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;

    public OperationLogController(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping({"", "/"})
    public String list(@RequestParam(defaultValue = "") String tableName,
                       @RequestParam(defaultValue = "") String operation,
                       @RequestParam(defaultValue = "") String keyword,
                       Model model) {
        String like = StringUtils.hasText(keyword) ? "%" + keyword + "%" : keyword;
        List<OperationLog> logs = operationLogMapper.selectFiltered(tableName, operation, like);
        model.addAttribute("logs", logs);
        model.addAttribute("tables", operationLogMapper.selectDistinctTables());
        model.addAttribute("filterTable", tableName);
        model.addAttribute("filterOperation", operation);
        model.addAttribute("filterKeyword", keyword);
        return "operation_log";
    }
}
