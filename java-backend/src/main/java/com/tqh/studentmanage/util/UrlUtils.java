package com.tqh.studentmanage.util;

import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 构造分页链接使用的查询串（形如 "&dept_id=D01&keyword=张"）。
 */
public final class UrlUtils {

    private UrlUtils() {
    }

    /**
     * @param parts 交替传入参数名和参数值
     */
    public static String queryString(Object... parts) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i + 1 < parts.length; i += 2) {
            Object value = parts[i + 1];
            if (value == null || !StringUtils.hasText(value.toString())) {
                continue;
            }
            items.add(parts[i].toString() + "=" + URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
        }
        return items.isEmpty() ? "" : "&" + String.join("&", items);
    }
}
