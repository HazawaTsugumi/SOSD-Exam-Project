package com.sosd.service;

import java.io.IOException;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.POJO.Like;
import com.sosd.domain.POJO.User;

public interface LikeService extends IService<Like>{
    
    public void changeLikeStatus(Long blogId,User user) throws IOException;
}
