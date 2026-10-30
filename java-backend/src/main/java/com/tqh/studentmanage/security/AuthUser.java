package com.tqh.studentmanage.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    private Integer id;
    private String username;
    private String realName;
    private String role;
    private String studentId;

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isAcademic() {
        return "academic".equals(role);
    }

    public boolean isReadonly() {
        return "readonly".equals(role);
    }

    public String getDisplayName() {
        return realName == null || realName.isBlank() ? username : realName;
    }
}
