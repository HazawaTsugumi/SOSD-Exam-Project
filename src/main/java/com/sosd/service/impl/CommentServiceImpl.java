package com.sosd.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BasicData;
import com.sosd.domain.DTO.CommentDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Comment;
import com.sosd.domain.POJO.User;
import com.sosd.mapper.CommentMapper;
import com.sosd.service.BasicDataService;
import com.sosd.service.CommentService;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper,Comment> implements CommentService{

    @Autowired
    private BasicDataService basicDataService;

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Override
    public PageResult getComment(Long blogId, int page, int size) {
        IPage<Comment> current = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getBlogId, blogId).eq(Comment::getParentId, -1);
        current = this.page(current, wrapper);
        List<CommentDTO> resDto = new ArrayList<>();

        for(Comment comment : current.getRecords()) {
            resDto.add(getCommentTree(comment.getId()));
        }

        PageResult p = new PageResult();
        p.setRows(resDto);
        p.setTotal(current.getTotal());
        return p;
    }

    private CommentDTO getCommentTree(Long commentId) {
        Comment parent = this.getById(commentId);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId);
        List<Comment> child = this.list(wrapper);
        List<CommentDTO> childList = new ArrayList<>();

        for(Comment comment : child) {
            childList.add(getCommentTree(comment.getId()));
        }

        return new CommentDTO(parent, childList);
    }

    @Override
    public void addComment(Comment comment,User user) throws IOException {
        comment.setId(null);
        comment.setUserId(user.getId());
        comment.setCreateTime(new Timestamp(System.currentTimeMillis()));
        this.save(comment);

        taskExecutor.execute(() -> {
            try {
                BasicData data = basicDataService.getBasicData();
                data.setCommentCount(data.getCommentCount() + 1);
                basicDataService.setBasicData(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void deleteComment(Long id,User user) throws IOException {
        
        Comment comment = this.getById(id);

        if(comment == null) {
            throw new BizException(MessageConstant.UNKNOWN_COMMENT);
        }

        if(comment.getUserId().longValue() != user.getId().longValue()){
            throw new BizException(MessageConstant.DELETE_AUTH_FAIL);
        }

        deleteChild(id);

        taskExecutor.execute(() -> {
            try {
                BasicData data = basicDataService.getBasicData();
                data.setCommentCount(this.count());
                basicDataService.setBasicData(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void deleteChild(Long id){

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, id);
        List<Comment> children = this.list(wrapper);

        for(Comment comment : children) {
            deleteChild(comment.getId());
        }

        this.removeById(id);
    }
}
