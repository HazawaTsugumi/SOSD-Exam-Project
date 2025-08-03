package com.sosd.controller;

import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BlogDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.Tag;
import com.sosd.service.BlogService;

import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
//文章相关接口
@RequestMapping("/blog")
public class BlogController {
    @Autowired
    private BlogService blogService;

    //根据标签分页查询相关文章,根据创建时间推送,tag为null就不根据标签查
    @GetMapping("/getBlogsByTag")
    public Result getBlogsByTag(String tag,int page,int size) {
        PageResult pageResult=blogService.getBlogsByTag(tag,page,size);
        return Result.success(pageResult);
    }

    //热门文章推荐,分页查询,标签
    @GetMapping("/getHotBlogsByTagOrNot")
    public Result getHotBlogs(String tag,int page,int size) {
        PageResult pageResult=blogService.getHotBlogs(tag,page,size);
        return Result.success(pageResult);
    }

    @GetMapping("/search")
    public Result search(String keyword,int page,int size) {
        PageResult pageResult=blogService.search(keyword,page,size);
        return Result.success(pageResult);
    }
    @PostMapping("/publish")
    public Result publish(@RequestBody BlogDTO blogDTO,@RequestHeader("Access-Token") String accessToken) {
        log.info("发布文章:{}",blogDTO.getTitle());
        blogService.publish(blogDTO,accessToken);
        return Result.success(null);
    }
    @GetMapping("/getTags")
    public Result getTags() {
        List<Tag> list =blogService.getTags();
        return Result.success(list);
    }
    @PostMapping("/postImage")
    public Result postImage(MultipartFile file) throws IOException {
        log.info("上传图片文件");
        String url=blogService.postImage(file);
        return Result.success(url);
    }

    @Autowired
    QwenStreamingChatModel chatModel;

    @GetMapping(value = "/ai/{id}",produces = "text/stream;charset=UTF-8")
    public Flux<String> aiConclude(@PathVariable("id") Long id){

        Blog blog = blogService.getById(id);
        if(blog == null) {
            throw new BizException(MessageConstant.UNKNOWN_BLOG);
        }

        String sanitizedContent = blog.getContent();

        return Flux.create(flux -> {
            chatModel.chat("根据文章内容总结一下这篇文章\n" + sanitizedContent, new StreamingChatResponseHandler() {

                @Override
                public void onCompleteResponse(ChatResponse arg0) {
                    flux.complete();
                }

                @Override
                public void onError(Throwable arg0) {
                    flux.error(arg0);
                }

                @Override
                public void onPartialResponse(String arg0) {
                    flux.next(arg0);
                }
                
            });
        });
    }

    @GetMapping("/detail/{id}")
    public Result getBlogById(@PathVariable("id") Long id){
        Blog blog = blogService.getBlogById(id);
        return Result.success(blog);
    }
}
