package com.sosd.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.User;
import com.sosd.service.ReadingRecordService;
import com.sosd.utils.JwtUtil;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/records")
public class ReadingRecordController {

    @Autowired
    private ReadingRecordService recordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;
    
    @GetMapping
    public Result getMethodName(@RequestParam("page") int page,@RequestParam("size") int size,@RequestHeader("Access-Token") String token) throws IOException {
        String userInfo = jwtUtil.getUserInfo(token);
        User user = objectMapper.readValue(userInfo, User.class);
        return Result.success(recordService.getReadingRecord(user, page, size));
    }
    
}
