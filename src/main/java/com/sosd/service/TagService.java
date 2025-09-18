package com.sosd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.POJO.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {
    void createTag(String tag);

    List<Tag> listAllEnabled();
}
