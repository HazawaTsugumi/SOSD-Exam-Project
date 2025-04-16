package com.sosd.domain.POJO;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 文章实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 * @author 应国浩
 */
@TableName("`blog`")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "blog")
public class Blog {
    
    /**
     * 文章id
     * 使用 Mybatis Plus 内置的雪花算法生成id
     */
    @Id
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文章标题
     */
    @Field(type = FieldType.Text)
    @TableField("`title`")
    private String title;

    /**
     * 文章作者的用户id
     */
    @TableField("`user_id`")
    @Field(type = FieldType.Long,index = false)
    private Long userId;

    /**
     * 文章内容（html格式）
     */
    @Field(type = FieldType.Text)
    @TableField("`content`")
    private String content;

    /**
     * 文章的点赞数
     */
    @Field(type = FieldType.Long,index = false)
    @TableField("`like`")
    private Long like;

    /**
     * 文章的创建时间
     */
    @Field(type = FieldType.Date,index = false)
    @TableField("`create_time`")
    private Timestamp createTime;

    /**
     * 文章的最近一次更新时间
     */
    @Field(type = FieldType.Date,index = false)
    @TableField("`update_time`")
    private Timestamp updateTime;

    /**
     * 文章的收藏量
     */
    @Field(type = FieldType.Long,index = false)
    @TableField("`collect`")
    private Long collect;

    /**
     * 文章的用户访问量UV，一个用户访问多次只记录一次
     */
    @Field(type = FieldType.Long,index = false)
    @TableField("`user_view`")
    private Long userView;

    /**
     * 文章的访问量PV，一个用户访问多次记录多次
     */
    @Field(type = FieldType.Long,index = false)
    @TableField("`page_view`")
    private Long pageView;

    @Field(type = FieldType.Keyword)
    @TableField("tag")
    private String tag;

    @Field(type = FieldType.Keyword,index = false)
    @TableField("user")
    private String user;

    @Field(type = FieldType.Long,index = false)
    @TableField("read")
    private Long read;

    @Field(type = FieldType.Long,index = false)
    @TableField("comment")
    private Long comment;
}
