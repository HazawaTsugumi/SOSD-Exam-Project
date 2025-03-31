package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签与文章的关系实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 * @author 应国浩
 */
@TableName("`tag_blog`")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagBlog {
    
    /**
     * 无意义的主键，仅区分不同的关系
     * 使用 Mybatis Plus 内置的雪花算法生成id
     */
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标签对应的文章id
     */
    @TableField("`blog_id`")
    private Long blog_id;

    /**
     * 文章对应的标签id
     */
    @TableField("`tag_id`")
    private Long tag_id;
}
