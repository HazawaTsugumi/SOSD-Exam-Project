package com.sosd.domain.VO;

import lombok.Data;
import org.springframework.cglib.core.Local;

import java.sql.Timestamp;

@Data
public class BlogVO {
    private Long id;
    private String title;
    private Timestamp createTime;
    private Long userId;
    private String content;
    //默认文章前50个字用于用户搜索文章时显示给用户看
    //当文章内容有用户查询的关键字时，高亮显示并将关键字所在片段前后共50字返回
    private String contentHead;
    private Long like;
    private String user;
    private Long read;
    private Long comment;
}
