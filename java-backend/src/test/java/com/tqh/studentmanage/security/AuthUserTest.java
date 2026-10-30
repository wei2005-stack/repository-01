package com.tqh.studentmanage.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUserTest {

    @Test
    @DisplayName("角色判断")
    void roleFlags() {
        AuthUser admin = new AuthUser(1, "admin", null, "admin", null);
        assertTrue(admin.isAdmin());
        assertFalse(admin.isAcademic());
        assertFalse(admin.isReadonly());

        AuthUser academic = new AuthUser(2, "teacher", null, "academic", null);
        assertTrue(academic.isAcademic());
        assertFalse(academic.isAdmin());

        AuthUser student = new AuthUser(3, "student", null, "readonly", "2023010101");
        assertTrue(student.isReadonly());
        assertEquals("2023010101", student.getStudentId());
    }

    @Test
    @DisplayName("显示名优先使用真实姓名，缺失时回退用户名")
    void displayNameFallback() {
        assertEquals("系统管理员", new AuthUser(1, "admin", "系统管理员", "admin", null).getDisplayName());
        assertEquals("admin", new AuthUser(1, "admin", "  ", "admin", null).getDisplayName());
        assertEquals("admin", new AuthUser(1, "admin", null, "admin", null).getDisplayName());
    }
}
