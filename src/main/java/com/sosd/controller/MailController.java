package com.sosd.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sosd.domain.DTO.Result;
import com.sosd.service.MailService;

import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * 用于处理收发邮件的控制类
 * @author 应国浩
 */
@RestController
@RequestMapping("/mail")
public class MailController {
    
    @Autowired
    private MailService mailService;

    /**
     * 用于登录请求的获取验证码
     * @param email
     * @return
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     */
    @GetMapping("/login")
    public Result sendMessageByLogin(@RequestParam("email") String email) throws UnsupportedEncodingException, MessagingException {

        mailService.sendCodeByLogin(email);

        return Result.success(null);
    }

    /**
     * 用于注册的获取验证码
     * @param email
     * @return
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     */
    @GetMapping("/register")
    public Result sendMessageByRegister(@RequestParam("email") String email) throws UnsupportedEncodingException, MessagingException {

        mailService.sendCodeByRegister(email);

        return Result.success(null);
    }
}
