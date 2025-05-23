package com.sosd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BlogDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.ImageBlog;
import com.sosd.domain.POJO.Tag;
import com.sosd.domain.POJO.User;
import com.sosd.domain.VO.BlogVO;
import com.sosd.mapper.BlogMapper;
import com.sosd.mapper.ImageBlogMapper;
import com.sosd.mapper.TagMapper;
import com.sosd.repository.BlogDao;
import com.sosd.service.BlogService;
import com.sosd.utils.JwtUtil;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class BlogServiceImpl implements BlogService {
    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    BlogDao blogDao;
    @Autowired
    TagMapper tagMapper;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    MinioClient minioClient;
    @Autowired
    ImageBlogMapper imageBlogMapper;

    @Override
    public PageResult getBlogsByTag(String tag, int page, int size) {
        Criteria criteria = new Criteria();
        if(tag!=null){
            criteria=new Criteria("tag").fuzzy(tag);
        }
        Sort sort=Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest=PageRequest.of(page,size,sort);
        return getBlogs(size,criteria,pageRequest);
    }
    public PageResult getBlogs(int size,Criteria criteria,PageRequest pageRequest) {
        Query query=new CriteriaQuery(criteria);
        //pageRequest没有设置参数也会有默认参数,即也会分页
        if(pageRequest!=null){
            query.setPageable(pageRequest);
        }
        SearchHits<Blog> response = elasticsearchTemplate.search(query, Blog.class);
        PageResult pageResult=new PageResult();
        pageResult.setTotal(response.getTotalHits());
        List<BlogVO> list=new ArrayList<>();
        for(int i=0;i<size;i++){
            Blog blog = response.getSearchHit(i).getContent();
            BlogVO blogVO=new BlogVO();
            BeanUtils.copyProperties(blog,blogVO);
            String content=blog.getContent();
            if(content.length()<=50){
                blogVO.setContentHead(content);
            }else{
                blogVO.setContentHead(content.substring(0,50));
            }
            list.add(blogVO);
        }
        pageResult.setRows(list);
        return pageResult;
    }
    //TODO:优化对热门文章的判断
    @Override
    public PageResult getHotBlogs(String tag,int page, int size) {
        Criteria criteria = new Criteria();
        if(tag!=null){
            criteria=criteria.and("tag").fuzzy(tag);
        }
        LocalDateTime now = LocalDateTime.now();
        criteria = criteria.and("createTime").between(now.minusDays(7), now);
        Sort sort = Sort.by(Sort.Direction.DESC, "like");
        PageRequest pageRequest=PageRequest.of(page,size,sort);
        return getBlogs(size,criteria,pageRequest);
    }

    //TODO:给查询结果评分，匹配度更高的优先返回
    //TODO:性能优化
    @Override
    public PageResult search(String keyword,int page,int size) {
        Criteria criteria = new Criteria("title").fuzzy(keyword).or("content").fuzzy(keyword);
        Query query = new CriteriaQuery(criteria);
        //TODO:测,记
        List<HighlightField> highlightFields=new ArrayList<>();
        highlightFields.add(new HighlightField("content"));
        highlightFields.add(new HighlightField("title"));
        HighlightQuery highlightQuery=new HighlightQuery(new Highlight(highlightFields),null);
        query.setHighlightQuery(highlightQuery);

        PageRequest pageRequest=PageRequest.of(page,size);
        query.setPageable(pageRequest);

        SearchHits<Blog> response = elasticsearchTemplate.search(query, Blog.class);
        PageResult pageResult=new PageResult();
        List<BlogVO> list=new ArrayList<>();
        for(int i=0;i<size;i++){
            Blog content = response.getSearchHit(i).getContent();
            BlogVO blogVO=new BlogVO();
            BeanUtils.copyProperties(content,blogVO);
            //TODO:headContent高亮显示关键字
            list.add(blogVO);
        }
        pageResult.setTotal(response.getTotalHits());
        pageResult.setRows(list);
        return pageResult;
    }
    @Transactional
    @Override
    public Long publish(BlogDTO blogDTO,String accessToken) {
        Blog blog=new Blog();
        BeanUtils.copyProperties(blogDTO,blog);

        String userInfo = jwtUtil.getUserInfo(accessToken);
        //TODO:测,记
        ObjectMapper objectMapper=new ObjectMapper();
        User user;
        try {
            user = objectMapper.readValue(userInfo, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        blog.setUserId(user.getId());
        blog.setUser(user.getName());

        blog.setLike(0L);
        blog.setCreateTime(new Timestamp(System.currentTimeMillis()));
        blog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        blog.setCollect(0L);
        blog.setUserView(0L);
        blog.setPageView(0L);
        blog.setRead(0L);
        blog.setComment(0L);
        try {
            blogMapper.insert(blog);
            blogDao.save(blog);
        } catch (Exception e) {
            blogDao.delete(blog);
            throw new BizException(MessageConstant.Publish_Error);
        }
        return blog.getId();
    }

    @Override
    public List<Tag> getTags() {
        return tagMapper.selectList(null);
    }

    @Override
    public void setImage(List<MultipartFile> files,String accessToken,Long blogId) {
        String userInfo = jwtUtil.getUserInfo(accessToken);
        ObjectMapper objectMapper=new ObjectMapper();
        User user;
        try {
            user = objectMapper.readValue(userInfo, User.class);
        } catch (JsonProcessingException e) {
            throw new BizException(MessageConstant.Set_Image_Error);
        }
        for(int i=0;i<files.size();i++){
            try {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(MessageConstant.Bucket_Name)
                        .object(user.getId() + "_" + i)
                        .build());
                String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .bucket(MessageConstant.Bucket_Name)
                        .object(user.getId() + "_" + i)
                        .build());
                ImageBlog imageBlog=ImageBlog.builder().blogId(blogId).order(i).url(url).build();
                imageBlogMapper.insert(imageBlog);
            } catch (Exception e) {
                throw new BizException(MessageConstant.Set_Image_Error);
            }
        }
    }

    @Override
    public List<String> getImage(Long blogId) {
        LambdaQueryWrapper<ImageBlog> queryWrapper = new LambdaQueryWrapper<ImageBlog>();
        queryWrapper.eq(ImageBlog::getBlogId, blogId);
        List<ImageBlog> imageBlogs = imageBlogMapper.selectList(queryWrapper);
        imageBlogs.sort(new Comparator<ImageBlog>() {
            @Override
            public int compare(ImageBlog o1, ImageBlog o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        List<String> list=new ArrayList<>();
        for(int i=0;i<imageBlogs.size();i++){
            list.add(imageBlogs.get(i).getUrl());
        }
        return list;
    }
}
