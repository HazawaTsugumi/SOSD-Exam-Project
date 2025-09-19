package com.sosd.domain.POJO;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BlogDTO;
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
@TableName("`blog`")
@Document(indexName = "blog")
@Data
@NoArgsConstructor
public class Blog implements Serializable {

    public Blog(Long userId, Timestamp createTime, Timestamp updateTime, String user, List<Tag> tag) {
        this.userId = userId;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.user = user;
        this.tag = tag;
    }

    public Blog(String content){
        this.content = content;
    }

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
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    @TableField("`title`")
    private String title;

    /**
     * 文章作者的用户id
     */
    @Field(type = FieldType.Long)
    @TableField("`user_id`")
    private Long userId;

    /**
     * 文章内容（html格式）
     */
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    @TableField("`content`")
    private String content;

    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    @TableField("abstract_content")
    private String abstractContent;

    /**
     * 文章的点赞数
     */

    @Field(type = FieldType.Long)
    @TableField("`like`")
    private Long like;

    /**
     * 文章的创建时间
     */
    //可用于查最新发布的文章
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second)
    @TableField("`create_time`")
    private Timestamp createTime;

    /**
     * 文章的最近一次更新时间
     */
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second)
    @TableField("`update_time`")
    private Timestamp updateTime;

    /**
     * 文章的收藏量
     */

    @Field(type = FieldType.Long)
    @TableField("`collect`")
    private Long collect;

    @TableField(exist = false)
    private List<ImageBlog> imageBlogs;

    @TableField(exist = false)
    private List<Tag> tag;


    //用户名称
    @Field(type = FieldType.Keyword,index = false)
    @TableField("`user`")
    private String user;

    @Field(type = FieldType.Long)
    @TableField("`read`")
    private Long read;

    @Field(type = FieldType.Long)
    @TableField("`comment`")
    private Long comment;


    public static Blog publish(Long userId, String user,BlogDTO blogDTO) {
        Blog blog = new Blog(userId,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                user,
                blogDTO.getTags());
        BeanUtils.copyProperties(blogDTO, blog,"id");
        if(blog.getAbstractContent()==null){
            blog.setAbstractContent(blog.getContent().substring(0, MessageConstant.Abstract_Content_Default_Size));
        }
        blog.setRead(0L);
        blog.setLike(0L);
        blog.setCollect(0L);
        blog.setComment(0L);
        return blog;
    }

    public static Blog update(BlogDTO blogDTO){
        if(blogDTO.getId()==null){
            throw new BizException("id不能为空");
        }
        Blog blog = new Blog();
        BeanUtils.copyProperties(blogDTO,blog);
        blog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return blog;
    }

    public static Blog deleteById(Long id){
        Blog blog = new Blog();
        blog.setId(id);
        return blog;
    }

}
