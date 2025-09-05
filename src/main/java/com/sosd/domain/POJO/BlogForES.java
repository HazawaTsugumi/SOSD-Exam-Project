package com.sosd.domain.POJO;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 文章实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "blog")
public class BlogForES {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String title;

    /**
     * 文章的点赞数
     */
    @Field(type = FieldType.Long,index = false)
    @TableField("`like`")
    private Long like;

    /**
     * 文章的创建时间
     */
    //可用于查最新发布的文章
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second)
    @TableField("`create_time`")
    private LocalDateTime createTime;

    /**
     * 文章的最近一次更新时间
     */
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second)
    @TableField("`update_time`")
    private LocalDateTime updateTime;

    /**
     * 文章的收藏量
     */
    @Field(type = FieldType.Long,index = false)
    @TableField("`collect`")
    private Long collect;

    /**
     * 文章的用户访问量UV，一个用户访问多次只记录一次
     */
    @Field(type = FieldType.Long)
    @TableField("`user_view`")
    private Long userView;

    /**
     * 文章的访问量PV，一个用户访问多次记录多次
     */
    @Field(type = FieldType.Long,index = false)
    @TableField("`page_view`")
    private Long pageView;

    @Field(type = FieldType.Keyword)
    @TableField("`tag`")
    private String tag;

    //用户名称
    @Field(type = FieldType.Keyword,index = false)
    @TableField("`user`")
    private String user;

    @Field(type = FieldType.Long,index = false)
    @TableField("`read`")
    private Long read;

    @Field(type = FieldType.Long,index = false)
    @TableField("`comment`")
    private Long comment;

    //文章摘要
    @Field(type = FieldType.Text)
    @TableField("`abstract_content`")
    private String abstractContent;

    public static BlogForES of(Blog blog) {
        BlogForES blogForES = new BlogForES();
        BeanUtils.copyProperties(blog, blogForES,"createTime","updateTime");
        blogForES.setCreateTime(blog.getCreateTime().toLocalDateTime());
        blogForES.setUpdateTime(blog.getUpdateTime().toLocalDateTime());
        return blogForES;
    }
}
