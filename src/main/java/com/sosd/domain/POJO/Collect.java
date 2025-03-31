package com.sosd.domain.POJO;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 * @author 应国浩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`collect`")
public class Collect {
    
    /**
     * 无意义的主键，仅用于区分不同的收藏
     * 使用 Mybatis Plus 内置的雪花算法自动生成
     */
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 收藏用户的id
     */
    @TableId("`user_id`")
    private Long userId;

    /**
     * 收藏的文章的id
     */
    @TableId("`blog_id`")
    private Long blogId;

    /**
     * 收藏时间
     */
    @TableId("`create_time`")
    private Timestamp createTime;
}
