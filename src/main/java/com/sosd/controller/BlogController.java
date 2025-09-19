package com.sosd.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BlogDTO;
import com.sosd.domain.DTO.PageDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.TagBlog;
import com.sosd.domain.VO.BlogVO;

import com.sosd.domain.POJO.User;
import com.sosd.domain.VO.PostImageVO;
import com.sosd.domain.query.UserBlogsQuery;
import com.sosd.mapper.TagBlogMapper;
import com.sosd.repository.BlogDao;
import com.sosd.service.BlogService;
import com.sosd.utils.JwtUtil;

import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
//文章相关接口
@RequestMapping("/blog")
public class BlogController {
    @Autowired
    private BlogService blogService;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private Cache<String,Blog> blogCache;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BlogDao blogDao;
    @Autowired
    private TagBlogMapper tagBlogMapper;
    @Autowired
    private MinioClient minioClient;

    //根据标签分页查询相关文章,根据创建时间推送
    @GetMapping("/getBlogsByTag")
    public Result getBlogsByTag(Long tagId,int page,int size) {
        PageResult pageResult =blogService.getBlogsByTag(tagId,page,size);
        return Result.success(pageResult);
    }

    //热门文章推荐,分页查询
    @GetMapping("/getHotBlogs")
    public Result getHotBlogs(int page,int size) {
        PageDTO<BlogVO> pageDTO=blogService.getHotBlogs(page,size);
        return Result.success(pageDTO);
    }

    @GetMapping("/search")
    public Result search(String keyword,int page,int size) {
        PageResult pageResult =blogService.search(keyword,page,size);
        return Result.success(pageResult);
    }

    //TODO:es把时间戳转为时间用的时区有误
    @PostMapping("/publish")
    public Result publish(@RequestBody BlogDTO blogDTO,@RequestHeader("Access-Token") String accessToken) {
        log.info("发布文章:{}",blogDTO.getTitle());
        blogService.publish(blogDTO,accessToken);
        return Result.success(null);
    }

    @PostMapping("/postImage")
    public Result postImage(MultipartFile file) throws IOException {
        log.info("上传图片文件");
        PostImageVO postImageVO=blogService.postImage(file);
        return Result.success(postImageVO);
    }

    @GetMapping("/image")
    public Result getImage(String fileName){
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

        return Result.success(url);
    }

    @PutMapping
    public Result updateBlog(@RequestBody BlogDTO blogDTO) {
        blogService.updateBlog(blogDTO);
        return Result.success(null);
    }

    @GetMapping("/ofUser")
    public Result getBlogsOfUser(@RequestHeader(value = "Access-Token") String token) {
        UserBlogsQuery query=new UserBlogsQuery();
        query.setUserId(token,jwtUtil,objectMapper);
        Page<Blog> page = query.toMPPage(new OrderItem().setColumn("update_time").setAsc(false));
        blogService.page(page, Wrappers.lambdaQuery(Blog.class).eq(Blog::getUserId,query.getUserId()));

        return Result.success(BlogVO.convertToVOForPage(page.getRecords()));
    }

    @DeleteMapping
    public Result deleteBlog(Long id){
        blogService.deleteBlog(id);
        return Result.success(null);
    }
    @DeleteMapping("/batch")
    @Transactional
    public Result deleteBlogs(String ids){
        if(ids!=null){
            List<Blog> list = Arrays.stream(ids.split(",")).map(id -> Blog.deleteById(Long.parseLong(id))).toList();
            boolean deleted = blogService.removeBatchByIds(list);
            //删除标签文章映射
            tagBlogMapper.delete(Wrappers.lambdaQuery(TagBlog.class)
                    .in(TagBlog::getBlogId,list.stream().map(Blog::getId).toList()));
            if(deleted){
                list.forEach(blog -> {blogDao.deleteById(blog.getId());
                deleteCache(blog.getId().toString());});
            }
        }
        return Result.success(null);
    }
    public void deleteCache(String id){
        redisTemplate.delete(id);
        blogCache.invalidate(id);
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


    

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/detail/{id}")
    public Result getBlogById(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Access-Token", required = false) String token) throws IOException{

        BlogVO blogVO;
        if(token == null){
            blogVO = blogService.getBlogById(id,null,true);
        }else{
            String userInfo = jwtUtil.getUserInfo(token);
            User user = objectMapper.readValue(userInfo, User.class);
            blogVO = blogService.getBlogById(id, user,true);
        }

        return Result.success(blogVO);
    }

}
