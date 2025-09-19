package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@Data
@TableName("image_blog")
public class ImageBlog {
    @TableId
    private Long id;

    @TableField("`order`")
    private Integer order;

    @TableField("`file_name`")
    private String fileName;

    @TableField(exist = false)
    private String url;


    public void generateUrl(RedisTemplate redisTemplate, MinioClient minioClient) {

        String url;
        url = (String) redisTemplate.opsForValue().get("image:"+fileName);
        if(url==null){
            try{
                url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .bucket(MessageConstant.SOSD_IMAGE)
                                .object(fileName)
                                .expiry(7, TimeUnit.DAYS)
                                .method(Method.GET)
                                .build());
                redisTemplate.opsForValue().set("image:"+fileName,url,7,TimeUnit.DAYS);
            }catch (Exception ex){
                throw new BizException("图片获取失败");
            }
        }
    }

}
