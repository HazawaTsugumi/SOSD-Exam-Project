package com.sosd.domain.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.Exception.BizException;
import com.sosd.domain.POJO.User;
import com.sosd.utils.JwtUtil;
import lombok.Data;
import lombok.Getter;


@Getter
public class UserBlogsQuery extends PageQuery {
    private Long userId;

    public void setUserId(String jwt, JwtUtil jwtUtil, ObjectMapper objectMapper) {
        try {
            User user = objectMapper.readValue(jwtUtil.getUserInfo(jwt), User.class);
            if(user==null){
                throw new BizException("用户信息解析失败");
            }
            this.userId = user.getId();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
