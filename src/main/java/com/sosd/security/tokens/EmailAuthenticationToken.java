package com.sosd.security.tokens;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * 定义新的 AuthenticationToken， 用来封装邮箱密码信息
 */
public class EmailAuthenticationToken extends AbstractAuthenticationToken{

    /**
     * 邮件
     */
    private final Object mail;

    /**
     * 验证码
     */
    private Object code;

    /**
     * 设置未认证构造器
     * @param email
     * @param code
     */
    public EmailAuthenticationToken(String email,String code){
        super(null);
        this.code = code;
        this.mail = email;
        setAuthenticated(false);
    }

    /**
     * 设置已认证构造器
     * @param email
     * @param code
     * @param authorities
     */
    public EmailAuthenticationToken(String email,String code,Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.code = code;
        this.mail = email;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.code;
    }

    @Override
    public Object getPrincipal() {
        return this.mail;
    }
    
}
