package com.sosd.domain.POJO;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`comment`")
public class Comment {
    
    /**
     * 评论id
     * 使用 Mybatis Plus 内置的雪花算法自动生成
     */
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 评论者的用户id
     */
    @TableField("`user_id`")
    private Long userId;

    /**
     * 评论所在的文章的文章id
     */
    @TableField("`blog_id`")
    private Long blogId;

    /**
     * 评论的父级评论的id
     * 如果这个评论回复了某一个评论，父级id则为被回复的那条评论的评论id
     * 如果这个评论并不是回复评论，则父级id为-1
     */
    @TableField("`parent_id`")
    private Long parentId;

    /**
     * 评论内容
     */
    @TableField("`content`")
    private String content;

    /**
     * 评论时间
     */
    @TableField("`create_time`")
    private Timestamp createTime;
}
