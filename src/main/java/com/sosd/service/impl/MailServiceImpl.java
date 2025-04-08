package com.sosd.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sosd.Exception.BizException;
import com.sosd.domain.POJO.User;
import com.sosd.service.MailService;
import com.sosd.service.UserService;
import com.sosd.utils.MailUtil;

import jakarta.mail.MessagingException;

/**
 * 实现邮件验证码接口的实现类
 * @author 应国浩
 */
@Service
public class MailServiceImpl implements MailService{

    @Autowired
    private UserService userService;

    @Autowired
    private MailUtil mailUtil;

    @Override
    public void sendCodeByLogin(String mail) throws UnsupportedEncodingException, MessagingException {
        
        //查询验证码对应的用户信息
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getEmail, mail);
        User user = userService.getOne(lambdaQueryWrapper);

        //如果用户不存在，则抛出异常
        if(user == null) {
            throw new BizException("该邮箱不存在");
        }

        int code = 0;

        //使用随机数生成验证码
        Random r = new Random();
        code = r.nextInt(1000000);

        //构造邮件消息，发出验证码邮件
        String message = "您的验证码是[" + String.format("%06d", code) + "],验证码有效期为五分钟，请尽快登录";
        mailUtil.sendMessage("登录验证码", message, mail);
    }
    
}
