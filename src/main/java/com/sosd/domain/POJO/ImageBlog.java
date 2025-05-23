package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;

@Data
@TableName("image_blog")
@Builder
public class ImageBlog {
    @TableId(value="blog_id",type = IdType.AUTO)
    private Long blogId;
    @TableField(value = "`order`")
    private int order;
    @TableField(value="`url`")
    private String url;
}
