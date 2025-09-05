package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 */

@TableName("`tag`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    
    /**
     * 标签id
     * 使用 Mybatis Plus 内置的雪花算法自动生成
     */
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标签名称
     */
    @TableField("`name`")
    private String name;

    /*
    * 0为禁用
    * 1为启用
    *
    * */

    @TableField("status")
    private byte status;

    public Tag(String name) {
        this.name = name;
    }

    public Tag(String name, byte status) {
        this.name = name;
        this.status = status;
    }

    public Tag(Long id, byte status) {
        this.id = id;
        this.status = status;
    }

}
