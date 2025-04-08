package com.sosd.domain.DO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sosd.domain.POJO.User;

import lombok.Getter;

/**
 * 用于 Spring Security 处理登录的实体类
 * @author 应国浩
 */
public class MyUserDetail implements UserDetails{

    /**
     * 用来登录或鉴权操作的对象
     */
    @Getter
    private final User user;

    @Getter
    private final String roleName;

    /**
     * 构造函数，这里不使用 Lombok 是因为导入的 RoleService 不应该由构造函数构造
     * @param user
     */
    public MyUserDetail(User user,String roleName){
        this.user = user;
        this.roleName = roleName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(roleName));
        return list;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
}
