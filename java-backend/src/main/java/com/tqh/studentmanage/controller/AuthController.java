package com.tqh.studentmanage.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tqh.studentmanage.entity.AppUser;
import com.tqh.studentmanage.mapper.AppUserMapper;
import com.tqh.studentmanage.security.AuthUser;
import com.tqh.studentmanage.security.JwtAuthenticationFilter;
import com.tqh.studentmanage.security.JwtTokenProvider;
import com.tqh.studentmanage.security.LoginUser;
import com.tqh.studentmanage.security.MigratingPasswordEncoder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppUserMapper appUserMapper;
    private final MigratingPasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          AppUserMapper appUserMapper,
                          MigratingPasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.appUserMapper = appUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletResponse response,
                          Model model) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            AuthUser user = ((LoginUser) authentication.getPrincipal()).getAuthUser();

            String token = jwtTokenProvider.createToken(user);
            Cookie cookie = new Cookie(JwtAuthenticationFilter.TOKEN_COOKIE, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) (jwtTokenProvider.getExpiration() / 1000));
            response.addCookie(cookie);

            upgradeLegacyPassword(user.getId(), password);
            return "redirect:/";
        } catch (AuthenticationException e) {
            model.addAttribute("error", "用户名不存在或密码错误");
            return "login";
        }
    }

    /**
     * 原系统使用 SHA-256 存储密码，登录成功后自动升级为 BCrypt。
     */
    private void upgradeLegacyPassword(Integer userId, String rawPassword) {
        AppUser stored = appUserMapper.selectById(userId);
        if (stored != null && passwordEncoder.isLegacy(stored.getPassword())) {
            appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                    .eq(AppUser::getId, userId)
                    .set(AppUser::getPassword, passwordEncoder.encode(rawPassword)));
        }
    }
}
