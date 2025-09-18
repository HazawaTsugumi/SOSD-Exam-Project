package com.sosd.SpringTask;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.sosd.domain.POJO.BeRead;
import com.sosd.domain.POJO.Blog;
import com.sosd.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class ReadUpdate {
    private final RedisTemplate redisTemplate;

    private final BlogService blogService;

    private static final int Cursor_Batch=100;

    private static final int Update_Batch=1000;

    private Cache<String, Blog> blogCache;

    @Scheduled(cron = "0 */10 * * * ?")
    public void readUpdate(){
        List<BeRead> list=new ArrayList<>();
        //多批次获取防止堵塞redis
        try (Cursor scan = redisTemplate.scan(ScanOptions.scanOptions().count(Cursor_Batch).match("blog:be_read:*").build())) {
            while (scan.hasNext()) {
                String key =(String) scan.next();
                if(key!=null){
                    Long id = Long.parseLong(key.split(":")[2]);
                    List popped = redisTemplate.opsForSet().pop(id, Integer.MAX_VALUE);
                    if(popped!=null){
                        list.add(new BeRead(id, popped.size()));
                    }
                }
            }
        }
        int hasUpdated=0;
        while(hasUpdated<list.size()){
            //数据库批量更新
            int from;
            int to;
            if((list.size()-hasUpdated)>Cursor_Batch){
                from=hasUpdated;
                hasUpdated+=Update_Batch;
                to=from+Update_Batch;
            }else{
                from=hasUpdated;
                hasUpdated+=list.size()-hasUpdated;
                to= list.size()-1;
            }
            List<BeRead> records = list.subList(from, to);
            for(int i=from;i<=to;i++){
                Long id=records.get(i).getId();
                blogService.update(Wrappers.lambdaUpdate(Blog.class)
                        .setSql("read = read + "+records.get(i).getCount())
                        .eq(Blog::getId, id));
                redisTemplate.delete(id.toString());
                blogCache.invalidate(id.toString());
            }
        }
    }
}
