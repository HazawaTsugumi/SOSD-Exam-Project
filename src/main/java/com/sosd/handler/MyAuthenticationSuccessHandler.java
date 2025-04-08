package com.sosd.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DO.MyUserDetail;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.User;
import com.sosd.utils.JwtUtil;
import com.sosd.utils.ResponsePrint;
import com.sosd.utils.TokenType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理登录认证成功的处理器
 * @author 应国浩
 */
@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler{

    /**
     * 用于输出结果类到前端
     */
    @Autowired
    private ResponsePrint responsePrint;

    /**
     * 使用 Jackson 将对象转化为字符串
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 生成 token 的工具类
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 当登录认证成功时的处理操作，包括输出成功信息和生成token
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        //构造结果类
        Result result = Result.success(null);

        //通过权限类获取对应的用户信息
        MyUserDetail userDetail = (MyUserDetail) authentication.getPrincipal();
        User user = userDetail.getUser();

        //将用户信息转化成 JSON 字符串并使用 token 工具生成 Token
        String userInfo = objectMapper.writeValueAsString(user);
        String accessToken = jwtUtil.generate(userInfo, TokenType.ACCESS);
        String refreshToken = jwtUtil.generate(userInfo, TokenType.REFRESH);

        //将生成的 token 放入响应头中
        response.setHeader("AccessToken", accessToken);
        response.setHeader("RefreshToken", refreshToken);

        //使用打印的工具输出结果到前端
        responsePrint.print(response, result);
    }
}
