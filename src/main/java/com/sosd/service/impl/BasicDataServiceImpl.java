package com.sosd.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DTO.BasicData;
import com.sosd.service.BasicDataService;
import com.sosd.service.BlogService;
import com.sosd.service.CollectService;
import com.sosd.service.CommentService;
import com.sosd.service.LikeService;
import com.sosd.service.UserService;

@Service
public class BasicDataServiceImpl implements BasicDataService{

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    @Lazy
    private BlogService blogService;

    @Autowired
    @Lazy
    private LikeService likeService;

    @Autowired
    @Lazy
    private CommentService commentService;

    @Autowired
    @Lazy
    private CollectService collectService;

    @Override
    public BasicData getBasicData() throws IOException {
        
        String data = redisTemplate.opsForValue().get("data:basic");
        BasicData basicData = new BasicData();
        if(data != null){
            basicData = objectMapper.readValue(data, BasicData.class);
        }else{
            basicData.setBlogCount(blogService.count());
            basicData.setCommentCount(commentService.count());
            basicData.setLikeCount(likeService.count());
            basicData.setUserCount(userService.count());
            basicData.setCollectCount(collectService.count());
            setBasicData(basicData);
        }
        return basicData;
    }

    @Override
    public void setBasicData(BasicData data) throws IOException {
        redisTemplate.opsForValue().set("data:basic", objectMapper.writeValueAsString(data));
    }

}
