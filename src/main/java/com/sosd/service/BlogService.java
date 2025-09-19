package com.sosd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.DTO.BlogDTO;
import com.sosd.domain.DTO.PageDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.BeRead;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.Tag;

import com.sosd.domain.VO.BlogVO;
import com.sosd.domain.VO.PostImageVO;
import com.sosd.domain.query.BlogsQuery;

import com.sosd.domain.POJO.User;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BlogService extends IService<Blog>{
    PageResult getBlogsByTag(Long tagId, int page, int size);

    PageDTO<BlogVO> getHotBlogs(int page,int size);

    PageResult search(String keyword, int page, int size);

    void publish(BlogDTO blogDTO,String accessToken);

//    List<Tag> getTags();

    PostImageVO postImage(MultipartFile file) throws IOException;

    BlogVO getBlogById(Long id, User user, boolean isDetail);

    void incrCollect(Long blogId, long delta);

    void incrLike(Long blogId, long delta);

    List<BlogVO> listByIds(List<Long> ids, User user);

    void updateBlog(BlogDTO blogDTO);

    void deleteBlog(Long id);

}
