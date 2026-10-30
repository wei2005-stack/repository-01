package com.tqh.studentmanage.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtTokenProviderTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 3_600_000L);

    private AuthUser sampleUser() {
        return new AuthUser(1, "admin", "系统管理员", "admin", null);
    }

    @Test
    @DisplayName("签发的 Token 可解析出用户信息")
    void createAndParseToken() {
        String token = provider.createToken(sampleUser());
        assertNotNull(token);

        AuthUser parsed = provider.parseToken(token);
        assertNotNull(parsed);
        assertEquals(1, parsed.getId());
        assertEquals("admin", parsed.getUsername());
        assertEquals("系统管理员", parsed.getRealName());
        assertEquals("admin", parsed.getRole());
    }

    @Test
    @DisplayName("非法 Token 返回 null")
    void invalidTokenReturnsNull() {
        assertNull(provider.parseToken("not-a-valid-token"));
        assertNull(provider.parseToken(null));
    }

    @Test
    @DisplayName("过期 Token 返回 null")
    void expiredTokenReturnsNull() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1_000L);
        String token = expiredProvider.createToken(sampleUser());
        assertNull(expiredProvider.parseToken(token));
    }
}
