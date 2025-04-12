package com.sosd.mapper;

import com.sosd.domain.POJO.Blog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogDao extends ElasticsearchRepository<Blog, Integer> {
}
