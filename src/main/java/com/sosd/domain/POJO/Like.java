package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 * @author 应国浩
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`like`")
public class Like {

    /**
     * 无意义的主键，仅区分不同的点赞
     * 使用 Mybatis Plus 内置的雪花算法自动生成
     */
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 被点赞的文章id
     */
    @TableField("`blog_id`")
    private Long blogId;

    /**
     * 点赞者的用户id
     */
    @TableField("`user_id`")
    private Long userId;
}
