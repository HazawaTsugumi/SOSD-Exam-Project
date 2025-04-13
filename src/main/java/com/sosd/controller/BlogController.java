package com.sosd.controller;

import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.DTO.Result;
import com.sosd.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
//文章相关接口
public class BlogController {
    @Autowired
    private BlogService blogService;

    //根据标签分页查询相关文章,根据创建时间推送
    @GetMapping
    public Result getBlogsByTag(String tag,int page,int size) {
        PageResult pageResult=blogService.getBlogsByTag(tag,page,size);
        return Result.success(pageResult);
    }

}
