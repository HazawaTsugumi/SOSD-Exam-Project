package com.sosd.controller;

import com.sosd.domain.DTO.Result;
import com.sosd.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Autowired
    TagService tagService;

    @PostMapping("/create")
    public Result create(String tag){
        tagService.createTag(tag);
        return Result.success(null);
    }
}
