package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 * @author 应国浩
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`user`")
public class User {
    
    /**
     * 用户id
     * 使用 Mybatis Plus 内置的雪花算法生成id
     */
    @TableId(type = IdType.AUTO,value = "`id`")
    private Long id;

    /**
     * 用户名
     */
    @TableField("`username`")
    private String username;

    /**
     * 密码
     * 将不会序列化为JSON字符串，防止密码泄露
     */
    @TableField("`password`")
    @JsonIgnore
    private String password;

    /**
     * 电子邮件
     */
    @TableField("`email`")
    private String email;

    /**
     * 该用户的角色，用于授权
     */
    @TableField("`role`")
    private Long role;

    /**
     * 该用户的名字
     */
    @TableField("`name`")
    private String name;
}
