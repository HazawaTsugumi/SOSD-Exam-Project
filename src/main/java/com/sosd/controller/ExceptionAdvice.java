package com.sosd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sosd.Exception.BizException;
import com.sosd.domain.DTO.Result;
import com.sosd.utils.MailUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 异常的处理类，当收到异常后，将执行这个代码
 * @author 应国浩
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @Autowired
    private MailUtil mailUtil;

    @Value("${my.admin-email}")
    private String adminEmail;
    
    /**
     * 如果抛出 BizException 直接返回错误结果，无需打印日志
     * @param exception
     * @return
     */
    @ExceptionHandler(BizException.class)
    public Result bizExceptionAdvice(BizException exception) {
        return Result.fail(exception.getMessage());
    }

    /**
     * 如果返回其他错误，则打印日志并邮件通知管理员
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionAdvice(Exception exception) {
        log.error(exception.getMessage());
        log.error("Stack Trace:" + exception.getStackTrace());
        try {
            mailUtil.sendMessage("服务器出现错误", exception.getMessage() + "\n" + "Stack Trace:" + exception.getStackTrace().toString(),adminEmail);
        } catch(Exception e) {
            log.error("发送邮件出现错误");
            log.error(exception.getMessage());
            log.error("Stack Trace:" + exception.getStackTrace().toString());
        }
        return Result.fail("出现错误，请联系管理员");
    }
}
