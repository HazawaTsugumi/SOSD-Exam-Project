package com.sosd.service.impl;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
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

        validEmail(mail);
        
        User user = selectUserByEmail(mail);

        //如果用户不存在，则抛出异常
        if(user == null) {
            throw new BizException("该邮箱不存在");
        }

        int code = generateCode(mail, "login");

        sendMessage(mail, code, "登录");
    }

    @Override
    public void sendCodeByRegister(String mail) throws UnsupportedEncodingException, MessagingException {

        validEmail(mail);
        
        User user = selectUserByEmail(mail);

        //如果用户存在，则抛出异常
        if(user != null) {
            throw new BizException("该邮箱已经被注册");
        }

        int code = generateCode(mail, "register");

        sendMessage(mail, code, "注册");
    }

    @Override
    public void sendCodeForForget(String mail) throws UnsupportedEncodingException, MessagingException {

        validEmail(mail);

        User user = selectUserByEmail(mail);

        //如果用户不存在，则抛出异常
        if(user == null) {
            throw new BizException("该邮箱不存在");
        }

        int code = generateCode(mail, "forget");

        sendMessage(mail, code, "重置密码");
    }
    
    /**
     * 查询验证码对应的用户信息
     * @param mail
     * @return
     */
    private User selectUserByEmail(String mail) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getEmail, mail);
        return userService.getOne(lambdaQueryWrapper);
    }

    /**
     * 使用随机数生成验证码
     * @param mail
     * @param prefix
     * @return
     */
    private int generateCode(String mail,String prefix){
        //检查redis中是否有验证码，如果有，继续发之前的验证码
        int code = 0;
        String cache = redisTemplate.opsForValue().get("mail:" + prefix +":" + mail);

        if(cache != null){
            code = Integer.parseInt(cache);
        }else{

            //如果没有，使用随机数生成验证码并存入redis中
            SecureRandom r = new SecureRandom();
            code = r.nextInt(1000000);
            redisTemplate.opsForValue().set("mail:" + prefix +":" + mail, Integer.toString(code));
            redisTemplate.expire("mail:" + prefix +":" + mail, 5,TimeUnit.MINUTES);
        }

        return code;
    }

    /**
     * 发送验证码邮件
     * @param mail
     * @param code
     * @param prefix
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     */
    private void sendMessage(String mail,int code,String prefix) throws UnsupportedEncodingException, MessagingException{
        //构造邮件消息，发出验证码邮件
        String message = "您正在进行" + prefix + "操作，验证码是[" + String.format("%06d", code) + "],验证码有效期为五分钟，请尽快登录";
        mailUtil.sendMessage("验证码", message, mail);
    }

    /**
     * 使用正则判断邮件是否合法，如果不合法，则抛出异常
     */
    private void validEmail(String mail){
        String regex = "/^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$/";
        if(mail == null || !mail.matches(regex)){
            throw new BizException("请输入正确的邮件格式");
        }
    }
}
