package com.sosd.service;

import com.sosd.domain.DTO.BlogDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BlogService {
    PageResult getBlogsByTag(String tag,int page,int size);

    PageResult getHotBlogs(String tag,int page,int size);

    PageResult search(String keyword,int page,int size);

    void publish(BlogDTO blogDTO,String accessToken);

    List<Tag> getTags();

    String postImage(MultipartFile file) throws IOException;
}
