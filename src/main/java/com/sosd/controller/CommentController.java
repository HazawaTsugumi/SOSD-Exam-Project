package com.sosd.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.Comment;
import com.sosd.domain.POJO.User;
import com.sosd.service.CommentService;
import com.sosd.utils.JwtUtil;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;
    
    @GetMapping("/get")
    public Result getComments(@RequestParam("blog_id") Long blogId,@RequestParam("page") int page,@RequestParam("size") int size){
        return Result.success(commentService.getComment(blogId, page, size));
    }

    @PostMapping("/add")
    public Result addComment(@RequestBody Comment comment,@RequestHeader("Access-Token") String token) throws IOException{
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);
        commentService.addComment(comment, user);
        return Result.success(null);
    }

    @DeleteMapping("/delete")
    public Result deleteComment(@RequestParam("id") Long id,@RequestHeader("Access-Token") String token) throws IOException{
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);
        commentService.deleteComment(id, user);
        return Result.success(null);
    }

    
}
