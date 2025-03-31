package com.sosd.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.domain.POJO.User;
import com.sosd.mapper.UserMapper;
import com.sosd.service.UserService;

/**
 * 用户服务接口的实现类
 * 使用 Mybatis Plus 自动实现接口
 * @author 应国浩
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService{
    
}
