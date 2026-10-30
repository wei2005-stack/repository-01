package com.tqh.studentmanage.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 兼容原 Python 版本生成的 SHA-256 密码，并在用户登录后自动升级为 BCrypt。
 */
public class MigratingPasswordEncoder implements PasswordEncoder {

    private static final String LEGACY_SALT = "student_manage_salt";
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        if (isLegacy(encodedPassword)) {
            return legacyHash(rawPassword).equalsIgnoreCase(encodedPassword);
        }
        return bcrypt.matches(rawPassword, encodedPassword);
    }

    public boolean isLegacy(String encodedPassword) {
        return encodedPassword != null && !encodedPassword.startsWith("$2");
    }

    private String legacyHash(CharSequence rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((LEGACY_SALT + rawPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
