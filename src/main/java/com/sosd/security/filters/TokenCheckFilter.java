package com.sosd.security.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sosd.domain.DTO.Result;
import com.sosd.utils.JwtUtil;
import com.sosd.utils.ResponsePrint;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用于请求时的 Token 校验
 * @author 应国浩
 */
@Component
public class TokenCheckFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ResponsePrint responsePrint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        //判断路径是否需要token，如果不需要，直接放行
        List<String> whiteList = new ArrayList<>();
        whiteList.add("/user/login/username");
        whiteList.add("/mail/login");
        whiteList.add("/user/login/mail");
        whiteList.add("/mail/register");

        //如果不需要，直接放行
        if(whiteList.contains(request.getRequestURI())){
            doFilter(request, response, filterChain);
            return;
        }

        //先获取 Token
        String token = request.getHeader("Access-Token");

        //如果 Token 没过期，直接放行
        if(jwtUtil.verify(token)){
            doFilter(request, response, filterChain);
            return;
        }

        //如果 Token 过期，输出错误信息给前端，让其调用刷新 token 的请求
        Result result = Result.fail("token过期", 400);
        responsePrint.print(response, result);
    }
    
}
