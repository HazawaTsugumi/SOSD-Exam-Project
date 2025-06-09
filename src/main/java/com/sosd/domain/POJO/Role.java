package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色的实体类，主要用来授权
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`role`")
public class Role {
    
    /**
     * 角色id
     * 使用 Mybatis Plus 内置的雪花算法自动生成
     */
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色名称
     */
    @TableField("`name`")
    private String name;
}
