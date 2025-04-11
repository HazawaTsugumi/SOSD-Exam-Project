package com.sosd.security.provider;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sosd.domain.POJO.Role;
import com.sosd.domain.POJO.User;
import com.sosd.security.tokens.EmailAuthenticationToken;
import com.sosd.service.RoleService;
import com.sosd.service.UserService;

/**
 * 自定义通过邮箱验证码认证过程
 * @author 应国浩
 */
@Component
public class EmailAuthenticationProvider implements AuthenticationProvider{

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        //从认证信息中获取邮箱和验证码
        String email = (String) authentication.getPrincipal();
        String code = (String) authentication.getCredentials();

        //从redis中获取验证码
        String cache = redisTemplate.opsForValue().get("mail:login:" + email);
        
        //如果redis中没存或验证码不正确，直接抛出异常
        if(cache == null || !cache.equals(code)){
            throw new BadCredentialsException("验证码错误或已过期");
        }

        //如果存在验证码则删除对应的验证码
        redisTemplate.delete("mail:login:" + email);

        //获取邮箱对应的用户
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getEmail, email);
        User user = userService.getOne(lambdaQueryWrapper);

        //获取该用户的角色信息，并存储到集合中
        Role role = roleService.getById(user.getRole());
        ArrayList<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(role.getName()));

        return new EmailAuthenticationToken(email, code, list);
    }

    @Override
    public boolean supports(Class<?> authentication) {

        //告诉 Spring Security 支持 EmailAuthenticationToken
        return EmailAuthenticationToken.class.isAssignableFrom(authentication);
    }
    
}
