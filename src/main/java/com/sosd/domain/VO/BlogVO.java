package com.sosd.domain.VO;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class BlogVO {
    private Long id;
    private String title;
    private Timestamp createTime;
    private Long userId;
    private String abstractContent;
    private Long like;
    private String user;
    private Long read;
    private Long comment;
}
