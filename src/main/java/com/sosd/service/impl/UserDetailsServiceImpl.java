package com.sosd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sosd.domain.DO.MyUserDetail;
import com.sosd.domain.POJO.User;
import com.sosd.service.UserService;


/**
 * Spring Security 内置的处理用户权限信息的 service 层接口的实现类
 * @author 应国浩
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService{

    /**
     * 自动注入获取用户的服务层代码
     */
    @Autowired
    private UserService userService;

    /**
     * 根据用户名获取对应的用户对象
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //根据用户名查询对应的用户
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername, username);
        User user = userService.getOne(lambdaQueryWrapper);

        //如果用户不存在，则抛出 UsernameNotFoundException
        if(user == null){
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        //返回创建好的用户对象
        return new MyUserDetail(user);
    }
    
}
