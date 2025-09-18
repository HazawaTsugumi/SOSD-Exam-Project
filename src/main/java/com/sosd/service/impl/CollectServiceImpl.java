package com.sosd.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sosd.domain.VO.BlogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BasicData;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.Collect;
import com.sosd.domain.POJO.User;
import com.sosd.mapper.CollectMapper;
import com.sosd.service.BasicDataService;
import com.sosd.service.BlogService;
import com.sosd.service.CollectService;

@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper,Collect> implements CollectService{

    @Autowired
    private BlogService blogService;

    @Autowired
    private BasicDataService basicDataService;

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Override
    @Transactional
    public void changeCollectStatus(Long blogId, User user) throws IOException{
        Blog blog = blogService.getById(blogId);
        if(blog == null) {
            throw new BizException(MessageConstant.UNKNOWN_BLOG);
        }
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getBlogId, blogId).eq(Collect::getUserId, user.getId());
        Long change = 0L;
        if(!this.exists(wrapper)){
            this.save(new Collect(null, user.getId(), blogId, new Timestamp(System.currentTimeMillis())));
            change = 1L;
        }else{
            this.remove(wrapper);
            change = -1L;
        }

        final Long finalChange = change;
        taskExecutor.execute(() -> {
            updateBasicData(finalChange);
        });

        blogService.incrCollect(blogId, change);
    }

    private void updateBasicData(Long change){
        try {
            BasicData data = basicDataService.getBasicData();
            data.setCollectCount(data.getCollectCount() + change);
            basicDataService.setBasicData(data);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResult getCollectList(User user, int page, int size) {
        
        IPage<Collect> current = new Page<>(page, size);

        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, user.getId()).orderByDesc(Collect::getCreateTime);

        current = this.page(current, wrapper);
        List<Collect> collects = current.getRecords();
        List<Long> ids = new ArrayList<>();
        for(Collect collect : collects) {
            ids.add(collect.getBlogId());
        }

        return new PageResult(current.getTotal()
                , blogService.list(Wrappers.lambdaQuery(Blog.class).in(Blog::getId,ids))
                                        .stream().map(BlogVO::convertToVO).toList());
    }
}
