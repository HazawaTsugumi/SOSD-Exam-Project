package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;


@Data
@TableName("tag_blog")
@Builder
public class TagBlog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @TableField("tag_id")
    private Long tagId;
    @TableField("blog_id")
    private Long blogId;
    //0:true  1:false
    @TableField("enabled")
    private Byte enabled;

    public static Byte ENABLED = 1;
    public static Byte DISABLED = 0;
}

