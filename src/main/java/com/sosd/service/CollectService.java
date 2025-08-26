package com.sosd.service;

import java.io.IOException;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Collect;
import com.sosd.domain.POJO.User;

public interface CollectService extends IService<Collect>{

    public void changeCollectStatus(Long blogId,User user) throws IOException;

    public PageResult getCollectList(User user,int page,int size);
}