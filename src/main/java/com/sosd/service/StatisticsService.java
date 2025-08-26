package com.sosd.service;

import java.sql.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.POJO.Statistics;
import com.sosd.domain.POJO.User;

public interface StatisticsService extends IService<Statistics>{
    
    public void addStatistics(User user);

    public List<Statistics> getStatistics(Date from,Date to);
}
