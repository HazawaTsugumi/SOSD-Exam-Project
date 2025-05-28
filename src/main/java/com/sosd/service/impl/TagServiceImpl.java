package com.sosd.service.impl;

import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.mapper.TagMapper;
import com.sosd.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl implements TagService {
    @Autowired
    TagMapper tagMapper;

    @Override
    public void createTag(String tag) {
        if(tag==null||tag.isEmpty()||tag.isBlank()){
            throw new BizException(MessageConstant.TAG_IS_IS_NULL);
        }
        tagMapper.insertTag(tag);
    }
}
