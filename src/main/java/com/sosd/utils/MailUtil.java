package com.sosd.utils;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 进行邮件发送的工具类
 */
@Component
public class MailUtil {
    
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    /**
     * 发送邮件的方法
     * @param title 邮件标题
     * @param content 邮件内容
     * @param to 邮件接收者
     * @throws MessagingException 
     * @throws UnsupportedEncodingException 
     */
    public void sendMessage(String title,String content,String to) throws MessagingException, UnsupportedEncodingException{

        //创建邮件和 MimeMessageHelper 对象
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,false);

        //设置邮件信息
        helper.setFrom(sender, "测试服务器");
        helper.setTo(to);
        helper.setSubject(title);
        helper.setText(content);

        //发送邮件
        javaMailSender.send(message);
    }
}
