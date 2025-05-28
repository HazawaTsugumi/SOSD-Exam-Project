package com.sosd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
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
            throw new BizException(MessageConstant.USERNAME_IS_USED);
        }

        //判断邮箱是否合法且不存在，如果不合法或已经被使用，抛出异常
        String email = user.getEmail();
        validEmail(email);
        if(selectByEmail(email) != null){
            throw new BizException(MessageConstant.EMAIL_IS_USED);
        }

        validateCode(email, code, "register");

        //如果正确，将密码加密，设置角色为普通用户
        user.setRole(3L);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //添加该用户到数据库表中，完成注册
        this.save(user);
    }

    @Override
    public void forgetPassword(User user, String code) {

        //判断邮箱是否合法以及存在，如果不合法或不存在，则抛出异常
        String email = user.getEmail();
        validEmail(email);
        User selectedUser = selectByEmail(email);
        if(selectedUser == null){
            throw new BizException(MessageConstant.EMAIL_NOT_FOUND);
        }

        //判断邮件验证码是否正确
        validateCode(email, code, "forget");

        //如果正确，重置密码为新输入的密码
        System.out.println(user.getPassword());
        selectedUser.setPassword(passwordEncoder.encode(user.getPassword()));
        this.updateById(selectedUser);
    }

    /**
     * 判断验证码是否正确
     * @param email
     * @param code
     * @param prefix
     */
    private void validateCode(String email,String code,String prefix){
        String cache = redisTemplate.opsForValue().get("mail:" + prefix + ":" + email);
        //如果验证码不正确，抛出异常
        if(cache == null || !cache.equals(code)){
            throw new BizException(MessageConstant.WRONG_VERIFY_CODE);
        }

        //如果正确，删除redis的内容
        redisTemplate.delete("mail:" + prefix + ":" + email);
    }

    /**
     * 根据邮箱获取用户信息
     * @param email
     * @return
     */
    private User selectByEmail(String email){

        //查询数据库表，判断邮箱是否存在
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getEmail, email);
        return this.getOne(lambdaQueryWrapper);
    }

    /**
     * 使用正则判断邮件是否合法，如果不合法，则抛出异常
     */
    private void validEmail(String mail){
        String regex = "^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if(mail == null || !mail.matches(regex)){
            throw new BizException(MessageConstant.WRONG_EMAIL_FORMAT);
        }
    }

    @Override
    public User getUserInfoById(Integer id) {
        User user = this.getById(id);
        if(user == null){
            throw new BizException(MessageConstant.USER_NOT_FOUND);
        }
        return user;
    }
}
