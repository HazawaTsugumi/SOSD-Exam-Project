package com.sosd.service;

import java.io.IOException;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Comment;
import com.sosd.domain.POJO.User;

public interface CommentService extends IService<Comment>{
    
    public PageResult getComment(Long blogId,int page,int size);

    public void addComment(Comment comment,User author) throws IOException;

    public void deleteComment(Long id,User user) throws IOException;
}
