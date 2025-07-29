package com.sosd.security.filters;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.User;
import com.sosd.security.tokens.EmailAuthenticationToken;
import com.sosd.service.UserService;
import com.sosd.utils.JwtUtil;
import com.sosd.utils.ResponsePrint;
import com.sosd.utils.TokenType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用于邮箱验证码登录的过滤器
 */
@Component
public class EmailCheckFilter extends AbstractAuthenticationProcessingFilter{

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ResponsePrint responsePrint;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 设置过滤器的处理url，以及认证成功处理器和认证失败处理器
     * @param manager
     */
    public EmailCheckFilter(AuthenticationManager manager){
        super("/user/login/mail");

        setAuthenticationSuccessHandler(
            (HttpServletRequest request,HttpServletResponse response,Authentication authentication) -> {

                //构造结果类
                Result result = Result.success(null);

                //获取用户信息
                String email = (String) authentication.getPrincipal();
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getEmail, email);
                User user = userService.getOne(queryWrapper);

                //使用 JWT 生成 token
                String userInfo = objectMapper.writeValueAsString(user);
                String refreshToken = jwtUtil.generate(userInfo, TokenType.REFRESH);
                String accessToken = jwtUtil.generate(userInfo, TokenType.ACCESS);

                //将token存入redis中方便执行退出登录操作
                redisTemplate.opsForValue().set("user:refreshToken:" + user.getId().toString(), refreshToken, TokenType.REFRESH.getTime(), TimeUnit.MILLISECONDS);
                redisTemplate.opsForValue().set("user:accessToken:" + user.getId().toString(), accessToken, TokenType.ACCESS.getTime(), TimeUnit.MILLISECONDS);

                //将 token 存入响应头，并打印响应
                response.setHeader("Access-Token", accessToken);
                response.setHeader("Refresh-Token", refreshToken);
                responsePrint.print(response, result);
            }
        );


        setAuthenticationFailureHandler(
            (HttpServletRequest request,HttpServletResponse response,AuthenticationException exception) -> {

                //生成错误的结果类
                Result result = Result.fail("验证码错误");

                //打印响应
                responsePrint.print(response, result);
            }
        );
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
