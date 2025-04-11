package com.sosd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sosd.domain.DTO.Result;
import com.sosd.domain.DTO.UserDTO;
import com.sosd.domain.POJO.User;
import com.sosd.service.UserService;

/**
 * 处理用户操作的控制类
 * @author 应国浩
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    /**
     * 处理用户注册的控制类
     * @param userDTO
     * @return
     */
    @GetMapping("/register")
    public Result register(UserDTO userDTO){

        //将 DTO 解构为 POJO 方便 Service 层处理业务
        User user = new User(null, userDTO.getUsername(), userDTO.getPassword(), userDTO.getEmail(), null, userDTO.getName());
        userService.register(user, userDTO.getCode());
        
        return Result.success(null);
    }
}
