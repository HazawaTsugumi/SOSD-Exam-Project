package com.sosd.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sosd.config.MyProperties;

/**
 * 用于生成 JWT，判断 JWT 是否过期以及解析 JWT 的工具类
 * @author 应国浩
 */
@Component
public class JwtUtil {
    
    /**
     * 从配置文件中获取密钥
     */
    @Autowired
    private MyProperties properties;

    /**
     * 根据用户信息生成 JWT
     * @param userInfo 用户信息
     * @param type token类型
     * @return 生成的 JWT
     */
    public String generate(String userInfo,TokenType type){

        //获取签名日期以及过期日期
        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + type.getTime());

        //生成默认的base64头内容
        Map<String,Object> map = new HashMap<>(16);
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        //最终生成 JWT
        return JWT.create()
            .withHeader(map)
            .withIssuer("sosd")
            .withIssuedAt(issuedDate)
            .withExpiresAt(expiredDate)
            .withClaim("userInfo", userInfo)
            .sign(Algorithm.HMAC256(properties.getSecret()));
    }

    /**
     * 判断 JWT 是否非法或过期
     * @param jwt
     * @return
     */
    public boolean verify(String jwt) {
        try {

            // 创建对应的判断器并确认 JWT 是否非法或过期
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(properties.getSecret())).build();
            jwtVerifier.verify(jwt);

            //如果未抛出异常，则说明该 JWT 合法且未过期
            return true;
        }catch (Exception e) {

            //如果抛出异常，则说明该 JWT 不合法或过期
            //我们不关心抛出的是什么异常，只需要知道该 JWT 过期即可
            return false;
        }
    }

    /**
     * 解码 JWT 并获取用户信息，用于从token获取用户信息
     * @param jwt
     * @return
     */
    public  String getUserInfo(String jwt) {

        //解码 JWT 并获取用户信息
        DecodedJWT decodedJWT = JWT.decode(jwt);
        return decodedJWT.getClaim("userInfo").asString();
    }
}
