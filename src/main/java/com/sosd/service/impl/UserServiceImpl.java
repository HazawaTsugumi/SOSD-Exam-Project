package com.sosd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.Exception.BizException;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void register(User user, String code) {
        
        //查询数据库表，判断username是否存在
        String username = user.getUsername();
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername, username);

        //如果是，说明账号重复，抛出异常
        if(this.getOne(lambdaQueryWrapper) != null){
            throw new BizException("该用户名已经存在，请修改后再试");
        }

        //如果不是，查询数据库表，判断邮箱是否存在
        String email = user.getEmail();
        lambdaQueryWrapper.or().eq(User::getEmail, email);

        //如果邮箱存在，说明邮箱重复，抛出异常
        if(this.getOne(lambdaQueryWrapper) != null){
            throw new BizException("该邮箱已经存在，请登录或更换邮箱");
        }

        //如果邮箱不存在，说明邮箱未注册
        //判断验证码是否正确
        String cache = redisTemplate.opsForValue().get("mail:register:" + email);
        System.out.println(cache);
        System.out.println(code);
        //如果验证码不正确，抛出异常
        if(cache == null || !cache.equals(code)){
            throw new BizException("验证码错误，请重试");
        }

        //如果正确，删除redis的内容，将密码加密，设置角色为普通用户
        redisTemplate.delete("mail:register:" + email);
        user.setRole(3L);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //添加该用户到数据库表中，完成注册
        this.save(user);
    }
    
}
