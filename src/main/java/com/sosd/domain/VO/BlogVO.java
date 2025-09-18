package com.sosd.domain.VO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sosd.domain.POJO.Blog;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
public class BlogVO {
    private Long id;
    private String title;
    private Timestamp createTime;
    private Long userId;
    //文章摘要,有高亮时为高亮内容,无时为文章前50字或者NLP算法提取摘要
    private String abstractContent;
    private String content;
    private Long like;
    private String user;
    private Long read;
    //private Long comment;
    //是否被读者点赞
    private Boolean isLiked=false;
    //是否被读者收藏
    private Boolean isCollected=false;

    public static BlogVO convertToVO(Blog blog) {
        BlogVO blogVO = new BlogVO();
        if(blog==null){
            throw new NullPointerException();
        }
        BeanUtils.copyProperties(blog,blogVO);
        return blogVO;
    }

    public static BlogVO convertToVOForPage(Blog blog) {
        BlogVO blogVO = new BlogVO();
        if(blog==null){
            throw new NullPointerException();
        }
        BeanUtils.copyProperties(blog,blogVO,"content");
        return blogVO;
    }

    public static List<BlogVO> convertToVOForPage(List<Blog> blogs) {
        if(blogs==null){
            throw new NullPointerException();
        }
        List<BlogVO> blogVOs = new ArrayList<>();
        for(Blog blog:blogs){
            BlogVO blogVO = BlogVO.convertToVOForPage(blog);
            blogVOs.add(blogVO);
        }
        return blogVOs;
    }

}
