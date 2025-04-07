package com.sosd.domain.DO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sosd.domain.POJO.Role;
import com.sosd.domain.POJO.User;
import com.sosd.service.RoleService;

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

    /**
     * 构造函数，这里不使用 Lombok 是因为导入的 RoleService 不应该由构造函数构造
     * @param user
     */
    public MyUserDetail(User user){
        this.user = user;
    }

    /**
     * 自动注入的 Service 类
     */
    @Autowired
    private RoleService roleService;

    /**
     * 获取用户的权限信息
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        //使用 Service 查询该角色对应的权限信息
        Role role = roleService.getById(user.getRole());

        //将权限名转换成权限对象并返回
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(role.getName()));
        return list;
    }

    /**
     * 获取用户的密码信息
     */
    @Override
    public String getPassword() {

        //获取该用户的密码
        String password = user.getPassword();

        //将密码设置为空，防止数据泄露；然后返回密码
        user.setPassword(null);
        return password;
    }

    /**
     * 获取该用户的用户名
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }
    
}
