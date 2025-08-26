package com.sosd.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.User;
import com.sosd.service.CollectService;
import com.sosd.utils.JwtUtil;

@RestController
@RequestMapping("/collect")
public class CollectController {
    
    @Autowired
    private CollectService collectService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @PutMapping("/status")
    public Result changeCollectStatus(@RequestParam("blog_id") Long blogId,@RequestHeader("Access-Token") String token) throws IOException {
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);
        collectService.changeCollectStatus(blogId, user);
        return Result.success(null);
    }

    @GetMapping("/history")
    public Result getCollectHistory(@RequestHeader("Access-Token") String token,@RequestParam("page") int page,@RequestParam("size") int size) throws IOException{
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);
        return Result.success(collectService.getCollectList(user, page, size));
    }
}
