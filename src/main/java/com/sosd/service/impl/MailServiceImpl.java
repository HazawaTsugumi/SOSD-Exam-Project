package com.sosd.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private StringRedisTemplate redisTemplate;

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

        //检查redis中是否有验证码，如果有，继续发之前的验证码
        int code = 0;
        String cache = redisTemplate.opsForValue().get("mail:login:" + mail);

        if(cache != null){
            code = Integer.parseInt(cache);
        }else{

            //如果没有，使用随机数生成验证码并存入redis中
            Random r = new Random();
            code = r.nextInt(1000000);
            redisTemplate.opsForValue().set("mail:login:" + mail, Integer.toString(code));
            redisTemplate.expire("mail:login:" + mail, 5,TimeUnit.MINUTES);
        }

        //构造邮件消息，发出验证码邮件
        String message = "您的验证码是[" + String.format("%06d", code) + "],验证码有效期为五分钟，请尽快登录";
        mailUtil.sendMessage("登录验证码", message, mail);
    }
    
}
