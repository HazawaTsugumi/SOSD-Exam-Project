package com.sosd.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sosd.security.filters.EmailCheckFilter;
import com.sosd.security.filters.JsonUsernamePasswordFilter;
import com.sosd.security.filters.TokenCheckFilter;

/**
 * 安全框架 Spring Security 的配置类
 * @author 应国浩
 */
@Configuration
public class SpringSecurityConfig {

    @Autowired
    private DaoAuthenticationProvider daoAuthenticationProvider;

    @Value("${my.white-list}")
    private List<String> whiteList;
    
    /**
     * 配置 Spring Security 的过滤器和拦截器以及放行接口
     * @param http 使用此进行配置
     * @return 配置好的过滤器链
     * @throws Exception 可能存在的错误我们直接抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,JsonUsernamePasswordFilter jsonUsernamePasswordFilter, EmailCheckFilter emailCheckFilter,TokenCheckFilter tokenCheckFilter) throws Exception {

        http

            //对于所有页面需要认证
            .authorizeHttpRequests(auth -> {
                auth
                    .requestMatchers(whiteList.toArray(new String[0])).permitAll()
                    .anyRequest().authenticated();
            })

            .authenticationProvider(daoAuthenticationProvider)

            //设置json字符串过滤器
            .addFilterAt(jsonUsernamePasswordFilter,UsernamePasswordAuthenticationFilter.class)

            //设置邮箱验证码登录过滤器
            .addFilterBefore(emailCheckFilter, JsonUsernamePasswordFilter.class)

            //设置检验 token 的过滤器
            .addFilterBefore(tokenCheckFilter, UsernamePasswordAuthenticationFilter.class)

            //设置登录的url，认证成功处理器，认证失败处理器
            .formLogin(login -> {
                login
                    .disable();
            })
            
            // 不创建会话，因为我们使用了 Token 而不是 Session
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            
            //同理，因为我们使用的是 Token 所以不需要 CSRF 防护
            .csrf(csrf -> {
                csrf.disable();
            });

        //构建配置好的过滤器链
        return http.build();
    }

    /**
     * 配置密码编码器
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        //返回默认的密码编码器便于后续登录注册的密码校验
        return new BCryptPasswordEncoder();
    }

    /**
     * 用于登录认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
}
