package com.sosd.controller;

import com.sosd.domain.DTO.BlogDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.Tag;
import com.sosd.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("publish")
    public Result publish(@RequestBody BlogDTO blogDTO,@RequestHeader("Access-Token") String accessToken) {
        log.info("发布文章:{}",blogDTO.getTitle());
        blogService.publish(blogDTO,accessToken);
        return Result.success(null);
    }
    @GetMapping("getTags")
    public Result getTags() {
        List<Tag> list =blogService.getTags();
        return Result.success(list);
    }

}
