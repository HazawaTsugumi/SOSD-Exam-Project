package com.sosd.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.DTO.UserDTO;
import com.sosd.domain.POJO.User;
import com.sosd.service.UserService;
import com.sosd.utils.JwtUtil;
import com.sosd.utils.TokenType;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * 处理用户操作的控制类
 * @author 应国浩
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 处理用户注册的控制类
     * @param userDTO
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO){

        //将 DTO 解构为 POJO 方便 Service 层处理业务
        User user = new User(null, userDTO.getUsername(), userDTO.getPassword(), userDTO.getEmail(), null, userDTO.getName());
        userService.register(user, userDTO.getCode());
        
        return Result.success(null);
    }

        /**
         * 根据 Refresh-Token 刷新 Access-Token 
         * @param refreshToken
         * @param response
         * @return
         */
         @PostMapping("/refresh")
         public Result refresh(@RequestHeader("Refresh-Token") String refreshToken,HttpServletResponse response) throws JsonMappingException, JsonProcessingException{

            //如果token过期，返回错误信息
            if(!jwtUtil.verify(refreshToken)){
                return Result.fail("登录认证已失效，请登录", -1);
            }

            //获取用户信息
            String userInfo = jwtUtil.getUserInfo(refreshToken);
            User user = objectMapper.readValue(userInfo, User.class);

            //获取redis的缓存，如果缓存未命中或不相等，则视为退出登录，返回错误信息
            String cache = redisTemplate.opsForValue().get("user:refreshToken:" + user.getId().toString());
            if(cache == null || !cache.equals(refreshToken)){
                return Result.fail("登录认证已失效，请登录", -1);
            }
            
            //生成新的token
            String newAccessToken = jwtUtil.generate(userInfo, TokenType.ACCESS);
            String newRefreshToken = jwtUtil.generate(userInfo, TokenType.REFRESH);
            response.setHeader("Access-Token", newAccessToken);
            response.setHeader("Refresh-Token", newRefreshToken);

            //将token存入redis中方便执行退出登录操作
            redisTemplate.opsForValue().set("user:refreshToken:" + user.getId().toString(), newRefreshToken, TokenType.REFRESH.getTime(), TimeUnit.MILLISECONDS);
            redisTemplate.opsForValue().set("user:accessToken:" + user.getId().toString(), newAccessToken, TokenType.ACCESS.getTime(), TimeUnit.MILLISECONDS);
            
            return Result.success(null);
        }

    /**
     * 用于重置密码操作
     * @param dto
     * @return
     */
    @PostMapping("/forget")
    public Result forgetPassword(@RequestBody UserDTO dto){

        User user = new User(null, null, dto.getPassword(), dto.getEmail(), null, null);
        userService.forgetPassword(user, dto.getCode());
        return Result.success(null);
    }

    /**
     * 根据token获取当前用户信息
     * @return
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     */
    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("Access-Token") String token) throws JsonMappingException, JsonProcessingException{

        //获取用户信息的JSON字符串，并反序列化为user对象
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);
        log.info(user.toString());
        user = userService.getById(user.getId());

        return Result.success(user);
    }

    /**
     * 根据id获取用户信息
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    public Result getUserInfo(@PathVariable("id") Integer id){
        
        return Result.success(userService.getUserInfoById(id));
    }

    @PostMapping("/logout")
    public Result logout(@RequestHeader("Access-Token") String token) throws JsonMappingException, JsonProcessingException{
        //获取用户信息的JSON字符串，并反序列化为user对象
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);
        redisTemplate.delete("user:accessToken:" + user.getId().toString());
        redisTemplate.delete("user:refreshToken:" + user.getId().toString());
        return Result.success(null);
    }
}
