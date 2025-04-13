package com.sosd.service.impl;

import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Blog;
import com.sosd.mapper.BlogDao;
import com.sosd.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl implements BlogService {
    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    BlogDao blogDao;

    @Override
    public PageResult getBlogsByTag(String tag, int page, int size) {
        Sort sort=Sort.by(Sort.Direction.DESC, "createTime");
        Criteria criteria=new Criteria("tag").fuzzy(tag);
        Query query= new CriteriaQuery(criteria);
        PageRequest pageRequest=PageRequest.of(page,size,sort);
        query.setPageable(pageRequest);
        SearchHits<Blog> search = elasticsearchTemplate.search(query, Blog.class);

        return null;
    }
}
