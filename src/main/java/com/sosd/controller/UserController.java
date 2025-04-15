package com.sosd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sosd.domain.DTO.Result;
import com.sosd.domain.DTO.UserDTO;
import com.sosd.domain.POJO.User;
import com.sosd.service.UserService;
import com.sosd.utils.JwtUtil;
import com.sosd.utils.TokenType;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 处理用户操作的控制类
 * @author 应国浩
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
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
    @GetMapping("/refresh")
    public Result refresh(@RequestHeader("Refresh-Token") String refreshToken,HttpServletResponse response){

        if(jwtUtil.verify(refreshToken)){
            String userInfo = jwtUtil.getUserInfo(refreshToken);
            String newAccessToken = jwtUtil.generate(userInfo, TokenType.ACCESS);
            String newRefreshToken = jwtUtil.generate(userInfo, TokenType.REFRESH);
            response.setHeader("Access-Token", newAccessToken);
            response.setHeader("Refresh-Token", newRefreshToken);
            return Result.success(null);
        }

        return Result.fail("登录认证已失效，请重试", -1);
    }
}
