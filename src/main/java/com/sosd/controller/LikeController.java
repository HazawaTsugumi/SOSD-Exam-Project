package com.sosd.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.User;
import com.sosd.service.LikeService;
import com.sosd.utils.JwtUtil;

@RestController
@RequestMapping("/like")
public class LikeController {
    
    @Autowired
    private LikeService likeService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @PutMapping("/status")
    public Result changeLikeStatus(@RequestParam("blog_id") Long blogId,@RequestHeader("Access-Token") String token) throws IOException {
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);
        likeService.changeLikeStatus(blogId, user);
        return Result.success(null);
    }
}
