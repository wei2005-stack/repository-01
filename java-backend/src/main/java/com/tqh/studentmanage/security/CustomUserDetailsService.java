package com.tqh.studentmanage.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tqh.studentmanage.entity.AppUser;
import com.tqh.studentmanage.mapper.AppUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserMapper appUserMapper;

    public CustomUserDetailsService(AppUserMapper appUserMapper) {
        this.appUserMapper = appUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, username)
                .eq(AppUser::getIsActive, true));
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在或已禁用");
        }
        AuthUser authUser = new AuthUser(user.getId(), user.getUsername(), user.getRealName(),
                user.getRole(), user.getStudentId());
        return new LoginUser(authUser, user.getPassword());
    }
}
