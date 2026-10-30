package com.tqh.studentmanage.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigratingPasswordEncoderTest {

    private static final String LEGACY_ADMIN_HASH =
            "0374c780983923ead6ba8404181e398db259e6dbeb291813afece285e493b314";

    private final MigratingPasswordEncoder encoder = new MigratingPasswordEncoder();

    @Test
    @DisplayName("原 SHA-256 密码可以校验通过")
    void matchesLegacyHash() {
        assertTrue(encoder.matches("admin123", LEGACY_ADMIN_HASH));
        assertTrue(encoder.matches("admin123", LEGACY_ADMIN_HASH.toUpperCase()));
        assertFalse(encoder.matches("wrong-password", LEGACY_ADMIN_HASH));
    }

    @Test
    @DisplayName("BCrypt 密码可正常加密与校验")
    void matchesBcrypt() {
        String encoded = encoder.encode("admin123");
        assertTrue(encoded.startsWith("$2"));
        assertTrue(encoder.matches("admin123", encoded));
        assertFalse(encoder.matches("admin124", encoded));
    }

    @Test
    @DisplayName("空密码或空 hash 返回 false")
    void rejectsNullInput() {
        assertFalse(encoder.matches("admin123", null));
    }

    @Test
    @DisplayName("可区分历史密码与 BCrypt 密码")
    void detectsLegacyFormat() {
        assertTrue(encoder.isLegacy(LEGACY_ADMIN_HASH));
        assertFalse(encoder.isLegacy(encoder.encode("admin123")));
    }
}
