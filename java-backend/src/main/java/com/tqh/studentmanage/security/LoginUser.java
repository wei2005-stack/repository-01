package com.tqh.studentmanage.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class LoginUser extends User {

    private final AuthUser authUser;

    public LoginUser(AuthUser authUser, String password) {
        super(authUser.getUsername(), password, buildAuthorities(authUser));
        this.authUser = authUser;
    }

    private static List<GrantedAuthority> buildAuthorities(AuthUser user) {
        return AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().toUpperCase());
    }

    public AuthUser getAuthUser() {
        return authUser;
    }
}
