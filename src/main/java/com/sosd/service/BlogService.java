package com.sosd.service;

import com.sosd.domain.DTO.PageResult;

public interface BlogService {
    PageResult getBlogsByTag(String tag,int page,int size);
}
