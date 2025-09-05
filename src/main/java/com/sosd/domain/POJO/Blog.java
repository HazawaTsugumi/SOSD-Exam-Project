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
 */
@TableName("`blog`")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blog {
    
    /**
     * 文章id
     * 使用 Mybatis Plus 内置的雪花算法生成id
     */

    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文章标题
     */

    @TableField("`title`")
    private String title;

    /**
     * 文章作者的用户id
     */

    @TableField("`user_id`")
    private Long userId;

    /**
     * 文章内容（html格式）
     */

    @TableField("`content`")
    private String content;

    /**
     * 文章的点赞数
     */

    @TableField("`like`")
    private Long like;

    /**
     * 文章的创建时间
     */
    //可用于查最新发布的文章

    @TableField("`create_time`")
    private Timestamp createTime;

    /**
     * 文章的最近一次更新时间
     */

    @TableField("`update_time`")
    private Timestamp updateTime;

    /**
     * 文章的收藏量
     */

    @TableField("`collect`")
    private Long collect;

    /**
     * 文章的用户访问量UV，一个用户访问多次只记录一次
     */

    @TableField("`user_view`")
    private Long userView;

    /**
     * 文章的访问量PV，一个用户访问多次记录多次
     */

    @TableField("`page_view`")
    private Long pageView;

    @TableField("`tag`")
    private String tag;

    //用户名称
    @TableField("`user`")
    private String user;

    @TableField("`read`")
    private Long read;

    @TableField("`comment`")
    private Long comment;

    //文章摘要
    @TableField("`abstract_content`")
    private String abstractContent;

    @TableField(exist = false)
    private Boolean isLiked;

    @TableField(exist = false)
    private Boolean isCollected;
}
