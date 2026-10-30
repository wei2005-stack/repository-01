package com.tqh.studentmanage.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlUtilsTest {

    @Test
    @DisplayName("按顺序拼接查询参数并进行 URL 编码")
    void buildsEncodedQueryString() {
        assertEquals("&deptId=D+01&keyword=%E5%BC%A0",
                UrlUtils.queryString("deptId", "D 01", "keyword", "张"));
    }

    @Test
    @DisplayName("空值、空白值与缺失值会被跳过")
    void skipsEmptyValues() {
        assertEquals("", UrlUtils.queryString());
        assertEquals("", UrlUtils.queryString("deptId", null, "keyword", ""));
        assertEquals("", UrlUtils.queryString("deptId", "   "));
        assertEquals("&page=2", UrlUtils.queryString("deptId", null, "page", 2));
    }
}
