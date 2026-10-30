package com.tqh.studentmanage.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * CSV 批量导入结果。
 */
@Data
public class ImportResult {

    private int success;
    private int failed;
    private int total;
    private List<String> errors = new ArrayList<>();

    public void addError(String message) {
        if (errors.size() < 10) {
            errors.add(message);
        }
    }
}
