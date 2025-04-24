package com.sosd.service;

import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;

/**
 * 用于发送邮件验证码的服务类
 * @author 应国浩
 */
public interface MailService {
    
    /**
     * 给指定邮件发送邮件验证码
     * @param mail
     * @throws MessagingException 
     * @throws UnsupportedEncodingException 
     */
    public void sendCodeByLogin(String mail) throws UnsupportedEncodingException, MessagingException;

    /**
     * 给指定邮件发送注册验证码
     * @param mail
     * @throws MessagingException 
     * @throws UnsupportedEncodingException 
     */
    public void sendCodeByRegister(String mail) throws UnsupportedEncodingException, MessagingException;

    /**
     * 忘记密码发送验证码
     * @param mail
     * @throws MessagingException 
     * @throws UnsupportedEncodingException 
     */
    public void sendCodeForForget(String mail) throws UnsupportedEncodingException, MessagingException;
}
