package com.sosd.domain.DTO;

import com.sosd.domain.POJO.ImageBlog;
import com.sosd.domain.POJO.Tag;
import lombok.Data;

import java.util.List;

@Data
public class BlogDTO {
    private Long id;
    private String title;
    private List<Tag> tags;
    private String content;
    private String abstractContent;
    private List<ImageBlog> imageBlogs;

}
