package com.sosd.security.filters;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DO.MyUserDetail;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.User;
import com.sosd.utils.JwtUtil;
import com.sosd.utils.ResponsePrint;
import com.sosd.utils.TokenType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于将json字符串转换为可供 Spring Security 识别的过滤器
 * @author 应国浩
 */
@Component
@Slf4j
public class JsonUsernamePasswordFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResponsePrint responsePrint;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 设置过滤器的处理url，以及认证成功处理器和认证失败处理器
     * @param authenticationFailureHandler
     * @param authenticationSuccessHandler
     */
    public JsonUsernamePasswordFilter(AuthenticationManager manager) {

        setFilterProcessesUrl("/user/login/username");

        //使用 Lambda 表达式设置认证成功处理器
        setAuthenticationSuccessHandler(
            (HttpServletRequest request,HttpServletResponse response,Authentication authentication) -> {
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
                response.setHeader("Access-Token", accessToken);
                response.setHeader("Refresh-Token", refreshToken);

                //使用打印的工具输出结果到前端
                responsePrint.print(response, result);
            }
        );

        // 使用 Lambda 表达式构建认证失败处理器
        setAuthenticationFailureHandler(
            (HttpServletRequest request,HttpServletResponse response,AuthenticationException exception) -> {

                //纪录日志
                log.info(exception.getMessage());

                //构建结果类
                Result result = Result.fail("用户名或密码错误");

                //输出结果类到前端
                responsePrint.print(response, result);
            }
        );


        setAuthenticationManager(manager);
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //获取请求信息并判断是否是json字符串
        String contentType = request.getContentType();
        if(contentType != null && (contentType.contains("application/json") || contentType.contains("application/json;charset=UTF-8"))){

            //如果是json字符串，则解析它为map对象
            try {
                Map<String,String> auth = objectMapper.readValue(request.getInputStream(), new TypeReference<Map<String,String>>() {});

                //获取用户名密码信息
                String username = auth.get("username");
                String password = auth.get("password");

                //新建一个Token对象，并传入用户名和密码
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
                setDetails(request, token);

                //认证成功
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
