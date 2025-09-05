package com.sosd.controller;

import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.Tag;
import com.sosd.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Autowired
    TagService tagService;

    @PostMapping
    public Result create(String tag){
        tagService.createTag(tag);
        return Result.success(null);
    }

    @GetMapping("/all")
    public Result getAll(){
        return Result.success(tagService.list());
    }

    @GetMapping("/all/enabled")
    public Result getAllEnabled(){
        return Result.success(tagService.listAllEnabled());
    }

    @DeleteMapping
    public Result delete(Long id){
        tagService.removeById(id);
        return Result.success(null);
    }

    @PutMapping
    public Result update(@RequestBody Tag tag){
        tagService.updateById(tag);
        return Result.success(null);
    }

    @PutMapping("/enable")
    public Result enable(Long id){
        Tag tag=new Tag(id, MessageConstant.ENABLE);
        tagService.updateById(tag);
        return Result.success(null);
    }

    //TODO:标签禁用时无法通过这个标签查到文章
    @PutMapping("/disable")
    public Result disable(Long id){
        Tag tag=new Tag(id, MessageConstant.DISABLE);
        tagService.updateById(tag);
        return Result.success(null);
    }

}
