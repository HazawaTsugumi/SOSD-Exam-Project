package com.sosd.service;

import com.sosd.domain.DTO.BlogDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BlogService {
    PageResult getBlogsByTag(String tag,int page,int size);

    PageResult getHotBlogs(String tag,int page,int size);

    PageResult search(String keyword,int page,int size);

    Long publish(BlogDTO blogDTO,String accessToken);

    List<Tag> getTags();

    void setImage(List<MultipartFile> files,String accessToken,Long blogId);

    List<String> getImage(Long blogId);
}
