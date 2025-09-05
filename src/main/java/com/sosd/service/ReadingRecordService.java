package com.sosd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.ReadingRecord;
import com.sosd.domain.POJO.User;

public interface ReadingRecordService extends IService<ReadingRecord>{
    
    public PageResult getReadingRecord(User user,int page,int size);
}
