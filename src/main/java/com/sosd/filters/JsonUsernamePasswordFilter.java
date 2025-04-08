package com.sosd.filters;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;

/**
 * 用于将json字符串转换为可供 Spring Security 识别的过滤器
 * @author 应国浩
 */
public class JsonUsernamePasswordFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * jackson 的json编码器和解码器
     */
    @Setter
    private ObjectMapper objectMapper;
    
    /**
     * 自定义的认证处理，用于处理json字符串请求的解析
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //获取请求信息并判断是否是json字符串
        String contentType = request.getContentType();
        System.out.println(contentType);
        if(contentType != null && (contentType.contains("application/json") || contentType.contains("application/json;charset=UTF-8"))){

            //如果是json字符串，则解析它为map对象
            try {
                Map<String,String> auth = objectMapper.readValue(request.getInputStream(), new TypeReference<Map<String,String>>() {});

                //获取用户名密码信息
                String username = auth.get("username");
                String password = auth.get("password");
                System.out.println(username);
                System.out.println(password);

                //新建一个Token对象，并传入用户名和密码
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
                setDetails(request, token);

                //让父类进行认证
                return this.getAuthenticationManager().authenticate(token);
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{

            //如果不是json字符串，则让父类调用
            return super.attemptAuthentication(request, response);
        }
    }
}
