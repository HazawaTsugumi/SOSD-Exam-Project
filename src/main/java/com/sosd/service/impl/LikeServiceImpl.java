package com.sosd.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BasicData;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.Like;
import com.sosd.domain.POJO.User;
import com.sosd.mapper.LikeMapper;
import com.sosd.service.BasicDataService;
import com.sosd.service.BlogService;
import com.sosd.service.LikeService;

@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper,Like> implements LikeService{

    @Autowired
    private BlogService blogService;

    @Autowired
    private BasicDataService basicDataService;

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Override
    @Transactional
    public void changeLikeStatus(Long blogId, User user) throws IOException{
        
        Blog blog = blogService.getById(blogId);
        if(blog == null) {
            throw new BizException(MessageConstant.UNKNOWN_BLOG);
        }

        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getBlogId, blogId).eq(Like::getUserId, user.getId());

        Long change = 0L;
        if(!this.exists(wrapper)){
            Like like = new Like(null, blogId, user.getId());
            this.save(like);
            change = 1L;
        }else{
            this.remove(wrapper);
            change = -1L;
        }

        final Long finalChange = change;
        taskExecutor.execute(() -> {
            updateBasicData(finalChange);
        });
        
        blogService.incrLike(blogId, finalChange);
    }
    
    private void updateBasicData(Long change){
        try {
            BasicData data = basicDataService.getBasicData();
            data.setLikeCount(data.getLikeCount() + change);
            basicDataService.setBasicData(data);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
