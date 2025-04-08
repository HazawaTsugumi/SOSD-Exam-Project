package com.sosd.security.filters;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.security.tokens.EmailAuthenticationToken;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用于邮箱验证码登录的过滤器
 * @author 应国浩
 */
@Component
public class EmailCheckFilter extends AbstractAuthenticationProcessingFilter{

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 设置过滤器的处理url，以及认证成功处理器和认证失败处理器
     * @param successHandler
     * @param failureHandler
     * @param manager
     */
    public EmailCheckFilter(AuthenticationSuccessHandler successHandler,AuthenticationFailureHandler failureHandler,AuthenticationManager manager){
        super("/user/login/mail");

        //TODO: 将认证成功处理器和认证失败处理器改成 Lambda 表达式
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
        setAuthenticationManager(manager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {

        //获取请求信息并判断是否是json字符串
        String contentType = request.getContentType();
        if(contentType != null && (contentType.contains("application/json") || contentType.contains("application/json;charset=UTF-8"))){

            //如果是json字符串，则解析它为map对象
            try {
                Map<String,String> auth = objectMapper.readValue(request.getInputStream(), new TypeReference<Map<String,String>>() {});

                //获取用户名密码信息
                String email = auth.get("email");
                String code = auth.get("code");

                //新建一个Token对象，并传入用户名和密码
                EmailAuthenticationToken token = new EmailAuthenticationToken(email, code);

                //获取认证结果
                return this.getAuthenticationManager().authenticate(token);
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException("无法获取到非JSON请求");
    }
    

}
