package com.sosd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.POJO.Tag;
import com.sosd.mapper.TagMapper;
import com.sosd.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper,Tag> implements TagService {
    @Autowired
    TagMapper tagMapper;

    @Override
    public void createTag(String tag) {
        if(tag==null||tag.isEmpty()||tag.isBlank()){
            throw new BizException(MessageConstant.TAG_IS_IS_NULL);
        }
        tagMapper.insert(new Tag(tag,MessageConstant.ENABLE));
    }

    @Override
    public List<Tag> listAllEnabled() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<Tag>();
        queryWrapper.eq(Tag::getStatus,1);
        return tagMapper.selectList(queryWrapper);
    }
}
