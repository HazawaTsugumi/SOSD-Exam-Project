package com.sosd.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 注册provider
 */
@Configuration
public class DaoAuthProviderConfig {
    
    @Autowired
    @Lazy
    private UserDetailsService userDetailsService;
    
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    /**
     * 注册DaoAuthenticationProvider认证提供者
     * @return DaoAuthenticationProvider认证提供者实例
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
