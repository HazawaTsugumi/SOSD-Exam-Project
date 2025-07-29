package com.sosd.security.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.config.MyProperties;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.User;
import com.sosd.utils.JwtUtil;
import com.sosd.utils.ResponsePrint;
import com.sosd.domain.DO.MyUserDetail;
import com.sosd.domain.POJO.Role;
import com.sosd.service.RoleService;
import com.sosd.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于请求时的 Token 校验
 */
@Component
@Slf4j
public class TokenCheckFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ResponsePrint responsePrint;

    @Autowired
    private ObjectMapper objectMapper;

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private MyProperties properties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        //从白名单中获取不需要token验证的uri
        String requestURI = request.getRequestURI();
        for(String item : properties.getWhitelist()){

            //如果与当前请求的uri相同，则放心
            if(pathMatcher.match(item, requestURI)){
                doFilter(request, response, filterChain);
                return;
            }
        }

        //如果都不匹配，则进行token校验
        //先获取 Token
        String token = request.getHeader("Access-Token");

        //如果 Token 过期，输出错误信息给前端，让其调用刷新 token 的请求
        if(token == null || !jwtUtil.verify(token)){
            Result result = Result.fail("token无效或缺失", 400);
            responsePrint.print(response, result);
            return;
        }

        //获取用户信息
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);

        //获取该用户在redis上的缓存
        String cache = redisTemplate.opsForValue().get("user:accessToken:" + user.getId().toString());

        //如果缓存未命中或不相等，说明用户已经退出登录或被其他人登录顶号
        if(cache == null || !cache.equals(token)){
            Result result = Result.fail("token过期", 400);
            responsePrint.print(response, result);
            return;
        }

        user = userService.getById(user.getId());

        //获取用户角色信息
        Role role = roleService.getById(user.getRole());
        MyUserDetail userDetail = new MyUserDetail(user, role.getName());

        //设置安全上下文，包含用户角色信息
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        //记录访问者信息
        log.info("用户访问: username={}, role={}, roleId={}, authorities={}, uri={}", 
            user.getUsername(), 
            role.getName(),
            role.getId(),
            userDetail.getAuthorities(),
            requestURI);

        doFilter(request, response, filterChain);
    }
    
}
