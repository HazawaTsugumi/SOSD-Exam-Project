package com.sosd.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.sosd.domain.DTO.Result;
import com.sosd.utils.ResponsePrint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理认证失败的处理器
 * @author 应国浩
 */
@Component
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler{

    /**
     * 用于输出结果类到前端
     */
    @Autowired
    private ResponsePrint responsePrint;

    /**
     * 处理登录认证失败
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        
        //构建结果类
        Result result = Result.fail("用户名或密码错误");

        //输出结果类到前端
        responsePrint.print(response, result);
    }
    
}
