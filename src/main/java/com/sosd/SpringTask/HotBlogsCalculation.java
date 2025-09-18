package com.sosd.SpringTask;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.sosd.domain.POJO.Blog;
import com.sosd.mapper.BlogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HotBlogsCalculation {

    private final RedisTemplate redisTemplate;

    public static final int Size = 100;

    private static final Long Gap = 1000*60*60*24L;

    private final BlogMapper blogMapper;

    public static final String Last_Blog_Id="LastBlogId";

    public static final String Hot_Blogs="HotBlogs";

    public static final ObjectMapper objectMapper = new ObjectMapper();

    private ElasticsearchTemplate elasticsearchTemplate;

    private final Cache<Integer,Blog> hotBlogsCache;

    @Scheduled(cron = "0 0 5 * * ?")
    public void hotBlogsCalculation() {
        PageRequest pageRequest = PageRequest
                .of(0, Size, Sort.by(Sort.Direction.DESC, "read"));
        NativeQuery nativeQuery = NativeQuery.builder().withPageable(pageRequest).build();
        SearchHits<Blog> search = elasticsearchTemplate.search(nativeQuery, Blog.class);
        List<Blog> list = search.getSearchHits().stream().map(SearchHit::getContent).toList();
        Set<ZSetOperations.TypedTuple<Blog>> set=new HashSet<>();
        for(int i=0;i<list.size();i++){
            ZSetOperations.TypedTuple<Blog> value= ZSetOperations.TypedTuple.of(list.get(i), (double) i);
            set.add(value);
            hotBlogsCache.put(i,list.get(i));
        }
        redisTemplate.opsForZSet().add(Hot_Blogs,set);

    }

}
