package com.sosd.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sosd.domain.VO.BlogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.ReadingRecord;
import com.sosd.domain.POJO.User;
import com.sosd.mapper.ReadingRecordMapper;
import com.sosd.service.BlogService;
import com.sosd.service.ReadingRecordService;

@Service
public class ReadingRecordServiceImpl extends ServiceImpl<ReadingRecordMapper,ReadingRecord> implements ReadingRecordService{

    @Autowired
    private BlogService blogService;

    @Override
    public PageResult getReadingRecord(User user, int page, int size) {

        IPage<ReadingRecord> current = new Page<>(page, size);
        
        LambdaQueryWrapper<ReadingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingRecord::getUserId, user.getId()).orderByDesc(ReadingRecord::getReadingTime);

        current = this.page(current, wrapper);
        List<BlogVO> blogs = new ArrayList<>();
        for(ReadingRecord record : current.getRecords()) {
            blogs.add(BlogVO.convertToVO(blogService.getOne(Wrappers.lambdaQuery(Blog.class).eq(Blog::getId,record.getBlogId()))));
        }

        return new PageResult(current.getTotal(),1L * page, blogs);
    }
    
}
