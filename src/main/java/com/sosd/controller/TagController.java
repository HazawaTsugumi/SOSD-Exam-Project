package com.sosd.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.DTO.TagDTO;
import com.sosd.domain.POJO.Tag;
import com.sosd.domain.POJO.TagBlog;
import com.sosd.mapper.TagBlogMapper;
import com.sosd.service.TagService;
import io.lettuce.core.StrAlgoArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag")
public class TagController {
    @Autowired
    TagService tagService;
    @Autowired
    TagBlogMapper tagBlogMapper;
    @Autowired
    SensitiveWordsController sensitiveWordsController;

    //删除文章标签
    @PutMapping("/blog/delete")
    public Result deleteTagOfBlog(Long blogId,Long tagId) {
        tagBlogMapper.delete(Wrappers.<TagBlog>lambdaQuery()
                .eq(TagBlog::getBlogId, blogId).eq(TagBlog::getTagId, tagId));
        return Result.success(null);
    }


    //给文章加已有标签
    @PutMapping("/blog/add")
    @Transactional
    public Result addTagToBlog(Long blogId,Long tagId) {
        //悲观锁
//        tagService.getOne(Wrappers.lambdaQuery(Tag.class).eq(Tag::getId, tagId).last("FOR UPDATE"));
        //加乐观锁
        Tag one = tagService.getOne(Wrappers.lambdaQuery(Tag.class).eq(Tag::getId, tagId));
        if(one.getStatus()==TagBlog.ENABLED){
            tagBlogMapper.insert(TagBlog.builder().blogId(blogId).tagId(tagId).enabled(TagBlog.ENABLED).build());
        }
        //空更新,若版本号变了抛异常回滚tagBlog表操作
        tagService.updateById(one);
        return Result.success(null);
    }

    //发布标签,用户或管理员均可
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
    //逻辑删除
    public Result delete(Long id){
        //若标签有被使用,下线标签,若没被使用,删除标签
        TagBlog tagBlog = tagBlogMapper.selectOne(Wrappers.lambdaQuery(TagBlog.class).eq(TagBlog::getTagId, id));
        if(tagBlog == null){
            tagService.removeById(id);
        }
        disable(id);
        return Result.success(null);
    }

    @DeleteMapping("/force")
    //强行删除
    public Result forceDelete(Long id){
        tagService.removeById(id);
        return Result.success(null);
    }
    //更新标签内容
    @PutMapping
    public Result update(@RequestBody TagDTO tagDTO){
        tagService.updateById(Tag.of(tagDTO));
        return Result.success(null);
    }

    @PutMapping("/enable")
    public Result enable(Long id){
        Tag tag=new Tag(id, MessageConstant.ENABLE);
        tagService.updateById(tag);
        tagBlogMapper.update(Wrappers.lambdaUpdate(TagBlog.class)
                .eq(TagBlog::getTagId,id).set(TagBlog::getEnabled,TagBlog.ENABLED));
        return Result.success(null);
    }

    @PutMapping("/disable")
    public Result disable(Long id){
        Tag tag=new Tag(id, MessageConstant.DISABLE);
        tagService.updateById(tag);
        tagBlogMapper.update(Wrappers.lambdaUpdate(TagBlog.class)
                .eq(TagBlog::getTagId,id).set(TagBlog::getEnabled,TagBlog.DISABLED));
        return Result.success(null);
    }

}
