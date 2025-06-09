package com.sosd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sosd.Exception.BizException;
import com.sosd.config.MyProperties;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.Result;
import com.sosd.utils.MailUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 异常的处理类，当收到异常后，将执行这个代码
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private MyProperties properties;
    
    /**
     * 如果抛出 BizException 直接返回错误结果，无需打印日志
     * @param exception
     * @return
     */
    @ExceptionHandler(BizException.class)
    public Result bizExceptionAdvice(BizException exception) {
        System.out.println(exception.getMessage());
        return Result.fail(exception.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public Result authorizationDeniedExceptionAdvice(AuthorizationDeniedException exception) {
        return Result.fail(MessageConstant.AUTH_FAIL,403);
    }

    /**
     * 如果返回其他错误，则打印日志并邮件通知管理员
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionAdvice(Exception exception) {
        log.error(exception.getMessage());
        log.error("Stack Trace:", exception);
        try {
            mailUtil.sendMessage("服务器出现错误", exception.getMessage() + "\n" + "Stack Trace:" + exception.getStackTrace().toString(),properties.getAdminEmail());
        } catch(Exception e) {
            log.error("发送邮件出现错误");
            log.error(exception.getMessage());
            log.error("Stack Trace:" + exception.getStackTrace().toString());
        }
        return Result.fail(MessageConstant.INTERNAL_ERROR);
    }
}
