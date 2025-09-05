package com.sosd.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.hankcs.hanlp.HanLP;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BasicData;
import com.sosd.domain.DTO.BlogDTO;

import com.sosd.domain.DTO.PageDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.*;

import com.sosd.domain.DTO.InteractionStatus;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.Collect;
import com.sosd.domain.POJO.Like;
import com.sosd.domain.POJO.ReadingRecord;
import com.sosd.domain.POJO.Tag;
import com.sosd.domain.POJO.User;

import com.sosd.domain.VO.BlogVO;
import com.sosd.domain.query.BlogsQuery;
import com.sosd.mapper.BlogMapper;

import com.sosd.mapper.TagBlogMapper;

import com.sosd.mapper.InteractionStatusMapper;

import com.sosd.mapper.TagMapper;
import com.sosd.repository.BlogDao;
import com.sosd.service.BasicDataService;
import com.sosd.service.BlogService;
import com.sosd.service.CollectService;
import com.sosd.service.LikeService;
import com.sosd.service.ReadingRecordService;
import com.sosd.service.StatisticsService;
import com.sosd.utils.JwtUtil;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchResult;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper,Blog> implements BlogService {
    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final Parser parser = Parser.builder().build();
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private BlogDao blogDao;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private BlogMapper blogMapper;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private TagBlogMapper tagBlogMapper;

    @Autowired
    private BasicDataService basicDataService;

    @Autowired
    private Cache<String,Blog> blogCache;

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
    public PageResult getBlogs(int size, Criteria criteria, PageRequest pageRequest) {
        Query query=new CriteriaQuery(criteria);
        //pageRequest没有设置参数也会有默认参数,即也会分页
        if(pageRequest!=null){
            query.setPageable(pageRequest);
        }
        SearchHits<Blog> response = elasticsearchTemplate.search(query, Blog.class);
        PageResult pageResult =new PageResult();
        pageResult.setTotal(response.getTotalHits());
        List<BlogVO> list=new ArrayList<>();
        for(int i=0;i<size;i++){
            Blog blog = response.getSearchHit(i).getContent();
            BlogVO blogVO=new BlogVO();
            BeanUtils.copyProperties(blog,blogVO);
            String content=blog.getContent();
//            if(content.length()<=50){
//                blogVO.setContent(content);
//            }else{
//                blogVO.setContent(content.substring(0,50));
//            }
            list.add(blogVO);
        }
        pageResult.setRows(list);
        return pageResult;
    }

    //TODO:优化对热门文章的判断
    @Override
    public PageDTO<BlogVO> getHotBlogs(BlogsQuery blogsQuery) {
        //TODO:多条件排序
        List<Tag> tags = blogsQuery.getTags();
        List<Long> tagIds = Collections.emptyList();
        if (tags != null && !tags.isEmpty()) {
            tagIds = tags.stream().map(tag -> {
                return tag.getId();
            }).toList();
        }
        List<Blog> blogs = blogMapper.selectPageByTag(tagIds);
        if (blogs == null || blogs.isEmpty()) {
            return PageDTO.empty();
        }
        Long total = (long) blogs.size();
        PageDTO<BlogVO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(total);
        Long pages=total % blogsQuery.getPageSize() ==0?total / blogsQuery.getPageSize():total / blogsQuery.getPageSize()+1;
        pageDTO.setPages(pages);
        List<Blog> hits = new ArrayList<>();
        if(total>blogsQuery.getPageSize()){
            for (int i = 0; i < blogsQuery.getPageSize(); i++) {
                hits.add(blogs.get(i));
            }
        }else{
            hits=blogs;
        }

        pageDTO.setList(hits.stream().map(blog -> {
            BlogVO blogVO = new BlogVO();
            BeanUtils.copyProperties(blog, blogVO);
            return blogVO;
        }).toList());

        return pageDTO;
    }

    //TODO:匹配度
    @Override
    public PageResult search(String keyword, int page, int size) {
        String title="title";
        String abstractContent="abstractContent";
        Criteria criteria = new Criteria(title).fuzzy(keyword).or(abstractContent).fuzzy(keyword);
        Query query = new CriteriaQuery(criteria);
        //TODO
        List<HighlightField> highlightFields=new ArrayList<>();
        highlightFields.add(new HighlightField(abstractContent));
        highlightFields.add(new HighlightField(title));
        //TODO:type
        HighlightQuery highlightQuery=new HighlightQuery(new Highlight(highlightFields),null);
        query.setHighlightQuery(highlightQuery);

        PageRequest pageRequest=PageRequest.of(page,size);
        query.setPageable(pageRequest);

        SearchHits<BlogForES> response = elasticsearchTemplate.search(query, BlogForES.class);
        List<BlogVO> list=new ArrayList<>();
        long total = response.getTotalHits();
        if(total>0){
            if(total<size){
                size= (int) total;
            }
            for(int i=0;i<size;i++){
                SearchHit<BlogForES> searchHit = response.getSearchHit(i);
                BlogForES content = searchHit.getContent();
                //es高亮处理关键字后会把处理后的字段放到HighlightFields
                Map<String, List<String>> highlightFieldMap = searchHit.getHighlightFields();
                List<String> titles = highlightFieldMap.get("title");
                if(titles!=null && !titles.isEmpty()){
                    content.setTitle(titles.get(0));
                }
                List<String> abstractContents = highlightFieldMap.get(abstractContent);
                if(abstractContents!=null && !abstractContents.isEmpty()){
                    content.setAbstractContent(abstractContents.get(0));
                }
                BlogVO blogVO=new BlogVO();
                BeanUtils.copyProperties(content,blogVO);
                list.add(blogVO);
            }
        }
        PageResult pageResult =new PageResult();
        pageResult.setTotal(total);
        pageResult.setRows(list);
        return pageResult;
    }

    @Override
    public void publish(BlogDTO blogDTO,String accessToken) {
        Blog blog=new Blog();
        BeanUtils.copyProperties(blogDTO,blog);
        String title=blog.getTitle();
        if(title==null|| title.isEmpty()||title.isBlank()){
            throw new BizException(MessageConstant.TITLE_IS_NULL);
        }

        String userInfo = jwtUtil.getUserInfo(accessToken);
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
        blog.setRead(0L);
        blog.setComment(0L);
        String content=blog.getContent();

        if(content==null||content.isBlank()|| content.isEmpty()){
            throw new BizException(MessageConstant.CONTENT_IS_NULL);
        }
        //将文本按段落拆分，每段获取一句摘要，然后拼接起来
        List<String> paragraphs = divideParagraphs(content);
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<paragraphs.size();i++){
            //TODO
            List<String> abstractParagraph = HanLP.extractSummary(paragraphs.get(i), 1);
            builder.append(abstractParagraph.get(0));
        }
        blog.setAbstractContent(builder.toString());
        //获取自创标签
        List<Tag> tags = blogDTO.getTags();
        List<Tag> newTags = tags.stream().filter(tag -> {
            return tag.getId() == null;
        }).toList();
        //TODO:检验标签是否合法


        try {
            String blogTag="";
            for(int i=0;i<tags.size();i++){
                blogTag+=tags.get(i).getName();
                if(i!=tags.size()-1){
                    blogTag+=",";
                }
            }
            blog.setTag(blogTag);
            blogMapper.insert(blog);
            //插入自创标签
            tagMapper.insert(newTags);
            //设置标签与文章映射
            tagBlogMapper.insert(blogDTO.getTags().stream().map(tag->{
                TagBlog tagBlog=new TagBlog();
                tagBlog.setTagId(tag.getId());
                tagBlog.setBlogId(blog.getId());
                return tagBlog;
            }).toList());
            BlogForES blogForES = BlogForES.of(blog);
            blogDao.save(blogForES);
        } catch (Exception e) {
            log.error(e.getMessage());
            blogDao.deleteById(blog.getId());
            throw new BizException(MessageConstant.PUBLISH_ERROR);
        }

        //管理端设置文章个数+1
        try{
            BasicData data = basicDataService.getBasicData();
            data.setBlogCount(data.getBlogCount() + 1L);
            basicDataService.setBasicData(data);
        }catch(IOException e) {
            throw new RuntimeException(e);
        }

        //添加文章时将文章数据加入缓存中
        blogCache.put(blog.getId().toString(), blog);
    }
    //TODO:记
    public List<String> divideParagraphs(String content){
        List<String> paragraphs=new ArrayList<>();
        Node document=parser.parse(content);
        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Paragraph paragraph) {
                TextContentRenderer renderer=TextContentRenderer.builder().build();
                String renderText = renderer.render(paragraph);
                paragraphs.add(renderText);
            }
        });
        return paragraphs;
    }


    @Override
    public List<Tag> getTags() {
        return tagMapper.selectList(null);
    }

    @Override
    //TODO:记
    public String postImage(MultipartFile file) throws IOException {
        if(file==null||file.isEmpty()){
            throw new BizException(MessageConstant.FILE_IS_NULL);
        }
        String fileName = file.getOriginalFilename();
        if(fileName==null||fileName.isEmpty()){
            throw new BizException(MessageConstant.FILENAME_IS_NULL);
        }
        String[] arr=fileName.split("\\.");
        String suffix=arr[arr.length-1];


        byte[] bytes = file.getBytes();
        //魔数最长为16位16进制数,而1字节为2位16进制,因此截取前8字节
        bytes = Arrays.copyOfRange(bytes, 0, MessageConstant.MAX_HEX_LENGTH / 2);
        String hexString = HexFormat.of().formatHex(bytes);


        if(!checkFileSortRight(suffix,hexString)){
            throw new BizException(MessageConstant.FILE_IS_NOT_IMAGE);
        }


        Snowflake snowflake= IdUtil.getSnowflake(0,0);
        long id=snowflake.nextId();

        File image=File.createTempFile(String.valueOf(id),suffix);
        file.transferTo(image);
        FileInputStream inputStream=new FileInputStream(image);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(MessageConstant.SOSD_IMAGE)
                    .object(id + "." + suffix)
                    .stream(inputStream, image.length(), -1)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException(MessageConstant.FAILED_UPLOAD);
        }finally {
            inputStream.close();
            boolean delete = image.delete();
            if(!delete){
                log.info(MessageConstant.FAILED_DELETE+":{}",image.getAbsolutePath());
            }
        }
        String url;
        try {
            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(MessageConstant.SOSD_IMAGE)
                    .object(id +"."+suffix)
                    .method(Method.GET)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException(MessageConstant.FAILED_GET);
        }

        return url;
    }


    public boolean checkFileSortRight(String suffix,String maxHexString){
        boolean suffixRight=false;
        for(String rightSuffix:MessageConstant.IMAGE_FILE_SUFFIXES){
            if (rightSuffix.equals(suffix)) {
                suffixRight = true;
                break;
            }
        }
        if(!suffixRight){
            return false;
        }

        boolean byteRight=false;
        for(Map.Entry<String,String> entry:MessageConstant.FILE_HEX_MAP.entrySet()){
            byte hexLengthOfMagicNumber = MessageConstant.getHexLengthOfMagicNumber(suffix);
            if(entry.getValue().equalsIgnoreCase(maxHexString.substring(0,hexLengthOfMagicNumber))){
                byteRight=true;
                break;
            }
        }
        return byteRight;
    }

    @Autowired
    @Lazy
    private LikeService likeService;

    @Autowired
    @Lazy
    private CollectService collectService;

    @Autowired
    @Lazy
    private ReadingRecordService readingRecordService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private InteractionStatusMapper interactionStatusMapper;

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Override
    public Blog getBlogById(Long id,User user,boolean isDetail) {
        Blog base = this.getById(id);

        if(base == null){
            throw new BizException(MessageConstant.UNKNOWN_BLOG);
        }

        Blog blog = new Blog();
        BeanUtils.copyProperties(base, blog);

        if(user == null){
            blog.setIsLiked(false);
            blog.setIsCollected(false);
        }else{

            InteractionStatus status = interactionStatusMapper.getStatus(user.getId(), id);
            blog.setIsCollected(status.isCollected());
            blog.setIsLiked(status.isLiked());

            if(isDetail){

                taskExecutor.execute(() -> {
                    addReadingRecord(id, user.getId());
                    this.updateById(base);
                });

                blog.setRead(blog.getRead() + 1);

                taskExecutor.execute(() -> {
                    base.setRead(blog.getRead());
                    this.updateById(base);
                });
            }
        }

        taskExecutor.execute(() -> {
            statisticsService.addStatistics(user);
        });

        return blog;
    }

    private void addReadingRecord(Long blogId,Long userId){
        LambdaQueryWrapper<ReadingRecord> readingWrapper = new LambdaQueryWrapper<>();
        readingWrapper.eq(ReadingRecord::getBlogId, blogId).eq(ReadingRecord::getUserId, userId);

        if(readingRecordService.exists(readingWrapper)){
            ReadingRecord readingRecord = readingRecordService.getOne(readingWrapper);
            readingRecord.setReadingTime(new Timestamp(System.currentTimeMillis()));
            readingRecordService.updateById(readingRecord);
        }else{
            readingRecordService.save(new ReadingRecord(null, userId, blogId, new Timestamp(System.currentTimeMillis())));
        }
    }

    public Blog getById(Long id) {

        Blog cache = blogCache.getIfPresent(id.toString());
        if(cache != null) {
            return cache;
        }

        Blog db = super.getById(id);
        if(db != null){
            blogCache.put(id.toString(), db);
        }
        return db;
    }

    public boolean updateById(Blog blog){
        blogCache.put(blog.getId().toString(), blog);
        return super.updateById(blog);
    }

    public void incrCollect(Long blogId, long delta) {

        this.update(
            null,
            new UpdateWrapper<Blog>()
                .setSql("collect = collect + " + delta)
                .eq("id", blogId)
        );
    
        blogCache.asMap().computeIfPresent(blogId.toString(), (k, v) -> {
            v.setCollect(v.getCollect() + (int) delta);
            return v;
        });
    }

    public void incrLike(Long blogId, long delta) {

        this.update(
            null,
            new UpdateWrapper<Blog>()
                .setSql("`like` = `like` + " + delta)
                .eq("id", blogId)
        );
    
        blogCache.asMap().computeIfPresent(blogId.toString(), (k, v) -> {
            v.setLike(v.getLike() + (int) delta);
            return v;
        });
    }

    @Override
    public List<Blog> listByIds(List<Long> ids,User user) {
        if(ids == null || ids.isEmpty()){
            return new ArrayList<>();
        }

        List<Blog> res = super.listByIds(ids);

        Map<Long, Blog> idToBlog = new HashMap<>();
        for (Blog blog : res) {
            idToBlog.put(blog.getId(), blog);
        }
        List<Blog> ordered = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Blog b = idToBlog.get(id);
            if (b != null) {
                ordered.add(b);
            }
        }

        if (user == null) {
            for (Blog blog : ordered) {
                blog.setIsLiked(false);
                blog.setIsCollected(false);
            }
            return ordered;
        }
        
        List<Long> likedIds = interactionStatusMapper.getLikedBlogIds(user.getId(), ids);
        List<Long> collectedIds = interactionStatusMapper.getCollectedBlogIds(user.getId(), ids);

        Set<Long> likedSet = new HashSet<>(likedIds);
        Set<Long> collectedSet = new HashSet<>(collectedIds);

        for (Blog blog : ordered) {
            blog.setIsLiked(likedSet.contains(blog.getId()));
            blog.setIsCollected(collectedSet.contains(blog.getId()));
        }

        return ordered;
    }
}
