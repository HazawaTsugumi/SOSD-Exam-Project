package com.sosd.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.POJO.Statistics;
import com.sosd.domain.POJO.User;
import com.sosd.mapper.StatisticsMapper;
import com.sosd.service.StatisticsService;

@Service
public class StatisticsServiceImpl extends ServiceImpl<StatisticsMapper,Statistics> implements StatisticsService{

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void addStatistics(User user) {
        
        redisTemplate.opsForValue().increment("data:pv", 1L);

        if(user == null){
            return;
        }
        redisTemplate.opsForSet().add("data:uv", String.valueOf(user.getId()));
    }

    @Override
    public List<Statistics> getStatistics(Date from, Date to) {

        if(from.after(to)){
            throw new BizException(MessageConstant.DATE_ERROR);
        }

        Date now = new Date(System.currentTimeMillis());

        if(to.after(now)){
            to = now;
        }

        LambdaQueryWrapper<Statistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Statistics::getDate, from, to);
        wrapper.orderByDesc(Statistics::getDate);
        List<Statistics> ans = this.list(wrapper);

        if(LocalDate.now().isEqual(to.toLocalDate())){
            ans.add(0, constructStatistics());
        }
        return ans;
    }

    private Statistics constructStatistics() {
        String pvCache = redisTemplate.opsForValue().get("data:pv");
        Long pv = pvCache != null ? Long.parseLong(pvCache) : 0;
        Long uv = redisTemplate.opsForSet().size("data:uv");
        return new Statistics(null, new Date(System.currentTimeMillis()), pv, uv);
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Shanghai")
    public void saveData(){
        String pvCache = redisTemplate.opsForValue().getAndSet("data:pv", "0");
        Long pv = (pvCache != null) ? Long.parseLong(pvCache) : 0L;
        
        Long uv = 0L;
        if (Boolean.TRUE.equals(redisTemplate.hasKey("data:uv"))) {
            redisTemplate.rename("data:uv", "data:uv:temp");
            uv = redisTemplate.opsForSet().size("data:uv:temp");
            redisTemplate.delete("data:uv:temp");
        }
        
        LocalDate statDate = LocalDate.now(ZoneId.of("Asia/Shanghai")).minusDays(1L);
        Statistics ans = new Statistics(null, Date.valueOf(statDate), pv, uv);
        
        this.save(ans);
    }
}