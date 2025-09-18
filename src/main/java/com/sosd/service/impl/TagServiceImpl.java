package com.sosd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.controller.SensitiveWordsController;
import com.sosd.domain.POJO.Tag;
import com.sosd.mapper.TagMapper;
import com.sosd.service.TagService;
import org.elasticsearch.threadpool.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper,Tag> implements TagService {
    @Autowired
    TagMapper tagMapper;
    @Autowired
    SensitiveWordsController sensitiveWordsController;
    private static final ExecutorService createTag= Executors.newFixedThreadPool(3);

    @Override
    public void createTag(String tag) {
        if(tag==null||tag.isEmpty()||tag.isBlank()){
            throw new BizException(MessageConstant.TAG_IS_IS_NULL);
        }
        if(sensitiveWordsController.judge(tag)){
            throw new BizException("标签包含敏感词");
        }
        //通过敏感词验证后,创建标签
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, tag);
        Tag tagEntity = tagMapper.selectOne(wrapper);
        if (tagEntity == null) {
            tagMapper.insert(new Tag(tag, MessageConstant.ENABLE));
        }
    }

    @Override
    public List<Tag> listAllEnabled() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<Tag>();
        queryWrapper.eq(Tag::getStatus,1);
        return tagMapper.selectList(queryWrapper);
    }
}
