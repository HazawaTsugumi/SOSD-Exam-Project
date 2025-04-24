package com.sosd.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sosd.domain.DTO.MessageSendDTO;
import com.sosd.domain.DTO.Result;
import com.sosd.service.MailService;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * 用于处理收发邮件的控制类
 * @author 应国浩
 */
@RestController
@RequestMapping("/mail")
@Slf4j
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
    @PostMapping("/login")
    public Result sendMessageByLogin(@RequestBody MessageSendDTO dto) throws UnsupportedEncodingException, MessagingException {

        mailService.sendCodeByLogin(dto.getEmail());

        return Result.success(null);
    }

    /**
     * 用于注册的获取验证码
     * @param email
     * @return
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     */
    @PostMapping("/register")
    public Result sendMessageByRegister(@RequestBody MessageSendDTO dto) throws UnsupportedEncodingException, MessagingException {

        mailService.sendCodeByRegister(dto.getEmail());

        return Result.success(null);
    }

    public Result sendMessageByForget(@RequestBody MessageSendDTO dto){
        
        return Result.success(null);
    }
}
