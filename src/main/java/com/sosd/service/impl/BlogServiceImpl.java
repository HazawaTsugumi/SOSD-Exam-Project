package com.sosd.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.sosd.Exception.BizException;
import com.sosd.SpringTask.HotBlogsCalculation;
import com.sosd.constant.MessageConstant;
import com.sosd.controller.SensitiveWordsController;
import com.sosd.domain.DTO.*;
import com.sosd.domain.POJO.*;
import com.sosd.domain.VO.BlogVO;
import com.sosd.mapper.BlogMapper;
import com.sosd.mapper.InteractionStatusMapper;
import com.sosd.mapper.TagBlogMapper;
import com.sosd.mapper.TagMapper;
import com.sosd.repository.BlogDao;
import com.sosd.service.*;
import com.sosd.utils.JwtUtil;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ExecutorService Blog_Count_Increment_Pool= java.util.concurrent.Executors.newFixedThreadPool(1);

    public static final String title="title";
    public static final String content="content";

    //懒加载
    private volatile ExecutorService Es_Retry_Pool;

    private void EsRetry(Runnable task) {
        if(Es_Retry_Pool==null){
            synchronized (this) {
                if(Es_Retry_Pool==null){
                    Es_Retry_Pool = Executors.newFixedThreadPool(1);
                }
            }
        }
        Es_Retry_Pool.execute(task);
    }

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
    @Autowired
    private SensitiveWordsController sensitiveWordsController;
    @Autowired
    private RedisTemplate redisTemplate;
    public static final String Hot_Blogs="HotBlogs";

    @Override
    //TODO:es的page页码从0开始,mp从1开始
    public PageResult getBlogsByTag(Long tagId, int page, int size) {
        //判断标签是否被禁用
        Tag tag = tagMapper.selectOne(Wrappers.lambdaQuery(Tag.class).eq(Tag::getId, tagId));
        if(tag==null){
            throw new BizException("标签不存在");
        }
        if(tag.getStatus()==MessageConstant.DISABLE){
            throw new BizException("标签已禁用");
        }

        //只查近一个月内的文章,按阅读量降序排列
        //TODO
//        Criteria criteria=new Criteria("tag")
//                .matches(tag.getName())
//                .and("createTime")
//                .greaterThan(Timestamp.valueOf((LocalDateTime.now().minusMonths(1))));
//        Sort sort= Sort.by(Sort.Direction.DESC, "read");
//        PageRequest pageRequest=PageRequest.of(page,size,sort);
//        return getBlogs(criteria,pageRequest);
        return null;
    }
    public PageResult getBlogs(Criteria criteria, PageRequest pageRequest) {
        Query query=new CriteriaQuery(criteria);
        //pageRequest没有设置参数也会有默认参数,即也会分页
        if(pageRequest!=null){
            query.setPageable(pageRequest);
        }
        SearchHits<Blog> response = elasticsearchTemplate.search(query, Blog.class);

        List<SearchHit<Blog>> searchHits = response.getSearchHits();
        List<BlogVO> rows=new ArrayList<>();
        for(int i=0;i<searchHits.size();i++){
            rows.add(BlogVO.convertToVOForPage(searchHits.get(i).getContent()));
        }
        return new PageResult(response.getTotalHits(),rows);
    }

    @Autowired
    private Cache<Integer,Blog> hotBlogsCache;

    @Override
    public PageDTO<BlogVO> getHotBlogs(int page,int size) {
        if(hotBlogsCache.getIfPresent(0)==null){
            log.info("从redis加载热门文章到缓存");
            //虽然为set类型但已经排序好了
            Set<Blog> range = redisTemplate.opsForZSet().range(Hot_Blogs, 0, -1);
            for(int i=0;i<range.size();i++){
                //每次获取都是新的迭代器
                Iterator<Blog> iterator = range.iterator();
                hotBlogsCache.put(i,iterator.next());
            }
        }
        int pages=HotBlogsCalculation.Size;
        if(pages<page*size){
            throw new BizException("页码超出");
        }
        int from =page*size;
        int to=from+size;
        List<BlogVO> rows=new ArrayList<>();
        for(int i=from;i<to;i++){
            rows.add(BlogVO.convertToVOForPage(hotBlogsCache.getIfPresent(i)));
        }
        return new PageDTO<>((long) pages, (long) (pages % size==0?pages/size:(pages/size+1)), rows);
    }

    //TODO:匹配度
    @Override
    public PageResult search(String keyword, int page, int size) {

        Criteria criteria = new Criteria(title).matches(keyword).or(content).matches(keyword);
        Query query = new CriteriaQuery(criteria);
        //TODO
        List<HighlightField> highlightFields=new ArrayList<>();
        highlightFields.add(new HighlightField(content));
        highlightFields.add(new HighlightField(title));
        //TODO:type
        HighlightQuery highlightQuery=new HighlightQuery(new Highlight(highlightFields),null);
        query.setHighlightQuery(highlightQuery);

        PageRequest pageRequest=PageRequest.of(page,size);
        query.setPageable(pageRequest);

        SearchHits<Blog> response = elasticsearchTemplate.search(query, Blog.class);
        List<BlogVO> list=new ArrayList<>();
        long total = response.getTotalHits();
        if(total>0){
            if(total<size){
                size= (int) total;
            }
            for(int i=0;i<size;i++){
                SearchHit<Blog> searchHit = response.getSearchHit(i);
                Blog result = searchHit.getContent();
                //es高亮处理关键字后会把处理后的字段放到HighlightFields
                Map<String, List<String>> highlightFieldMap = searchHit.getHighlightFields();
                List<String> titles = highlightFieldMap.get(title);
                if(titles!=null && !titles.isEmpty()){
                    //get(0)是获取整个title里的第1个keyword高亮时的title
                    result.setTitle(titles.get(0));
                }
                List<String> contents = highlightFieldMap.get(content);
                if(contents!=null && !contents.isEmpty()){
                    result.setAbstractContent(contents.get(0));
                }
                list.add(BlogVO.convertToVOForPage(result));
            }
        }
        return new PageResult(total,total%size==0?total/size:(total/size+1),list);
    }


    private static final long Retry_Interval = 5000;

    private static final int Max_Retry_Count=3;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public void publish(BlogDTO blogDTO, String accessToken) {
        //空判断
        String title = blogDTO.getTitle();
        if (title == null || title.isEmpty() || title.isBlank()) {
            throw new BizException(MessageConstant.TITLE_IS_NULL);
        }


        // String userInfo = jwtUtil.getUserInfo(accessToken);
        // User user;
        // try {
        //     user = objectMapper.readValue(userInfo, User.class);
        // } catch (JsonProcessingException e) {
        //     throw new RuntimeException(e);
        // }
        // blog.setUserId(user.getId());
        // blog.setUser(user.getName());


        // blog.setLike(0L);
        // blog.setCreateTime(new Timestamp(System.currentTimeMillis()));
        // blog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        // blog.setCollect(0L);
        // blog.setRead(0L);
        // blog.setComment(0L);
        // String content=blog.getContent();

        // if(content==null||content.isBlank()|| content.isEmpty()){

        //获取用户信息
        User user = jwtUtil.getUser(accessToken);
        String content = blogDTO.getContent();
        if (content == null || content.isBlank() || content.isEmpty()) {

            throw new BizException(MessageConstant.CONTENT_IS_NULL);
        }
        //构建blog对象
        Blog blog = Blog.publish(user.getId(), user.getName(), blogDTO);

        List<Tag> tagList = blogDTO.getTags();
        List<Long> tagIds=null;
        if(tagList!=null && !tagList.isEmpty()){
            tagIds= tagList.stream().map(Tag::getId).toList();
        }
        boolean allEnabled=true;
        List<Tag> tags=null;
        if(tagIds!=null && !tagIds.isEmpty()){
            tags = tagMapper.selectList(Wrappers.<Tag>lambdaQuery().in(Tag::getId, tagIds));
            if(tags==null || tags.isEmpty()){
                throw new BizException("服务器异常");
            }
            //乐观锁,防止发布过程标签被下线,但还是发布成功
            for (Tag tag : tags) {
                if (tag.getStatus() == TagBlog.DISABLED) {
                    allEnabled = false;
                    break;
                }
            }
        }
        if(!allEnabled){
            throw new BizException("所用标签状态变化,请重新选择标签");
        }
        blogMapper.insert(blog);
        //设置标签与文章映射
        assert tags != null;
        tagBlogMapper.insert(tags.stream().map(tag ->
                TagBlog.builder().tagId(tag.getId()).blogId(blog.getId()).enabled(TagBlog.ENABLED).build())
                .toList());
        //版本号变更就报错
        for(Tag tag : tags){
            tagMapper.updateById(tag);
        }


        try {
            blogDao.save(blog);
        } catch (Exception e) {
            log.error(e.getMessage());
            Runnable retry = () -> {
                int retryCount = 0;
                boolean success = false;
                while (retryCount < Max_Retry_Count) {
                    try {
                        blogDao.save(blog);
                        success = true;
                        break;
                    } catch (Exception ex) {
                        log.error("第:{}重试失败",retryCount+1);
                    }finally {
                        retryCount++;
                    }
                    try {
                        Thread.sleep(Retry_Interval);
                    } catch (InterruptedException ex) {
                        log.error("重试被尝试打断");
                    }
                }
                if(!success){
                    //本地重试失败,丢给消息队列
                    rabbitTemplate.convertAndSend("retry.direct","publish.retry",blog);
                }
            };
            EsRetry(retry);
        }

        //管理端设置文章个数+1
        Blog_Count_Increment_Pool.submit(()->{
            try{
                BasicData data = basicDataService.getBasicData();
                data.setBlogCount(data.getBlogCount() + 1L);
                basicDataService.setBasicData(data);
            }catch(IOException e) {
                throw new RuntimeException(e);
            }
        });
    }



//    @Override
//    public List<Tag> getTags() {
//        return tagMapper.selectList(null);
//    }

    @Override
    //TODO:
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

        File image= File.createTempFile(String.valueOf(id),suffix);
        file.transferTo(image);
        try (FileInputStream inputStream = new FileInputStream(image)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(MessageConstant.SOSD_IMAGE)
                    .object(id + "." + suffix)
                    .stream(inputStream, image.length(), -1)
                    .build());
        } catch (Exception e) {
            throw new BizException(MessageConstant.FAILED_UPLOAD);
        } finally {
            boolean delete = image.delete();
            if (!delete) {
                log.info(MessageConstant.FAILED_DELETE + ":{}", image.getAbsolutePath());
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

    public  void deleteCache(String id){
        redisTemplate.delete(id);
        blogCache.invalidate(id);
    }

    @Override
    @Transactional
    public void updateBlog(BlogDTO blogDTO) {
        //更改文章标签有单独接口
        blogDTO.setTags(null);
        Blog newBlog = Blog.update(blogDTO);
        boolean updated = updateById(newBlog);
        if (updated) {
            //更新失败策略
            try {
                elasticsearchTemplate.update(newBlog);
            } catch (Exception e) {
                log.error(e.getMessage());
                Runnable retry = () -> {
                    int retryCount = 0;
                    boolean success = false;
                    while (retryCount < Max_Retry_Count) {
                        try {
                            elasticsearchTemplate.update(newBlog);
                            success = true;
                            break;
                        } catch (Exception ex) {
                            log.error("第:{}重试失败", retryCount + 1);
                        } finally {
                            retryCount++;
                        }
                        try {
                            Thread.sleep(Retry_Interval);
                        } catch (InterruptedException ex) {
                            log.error("重试被尝试打断");
                        }
                    }
                    if (!success) {
                        //本地重试失败,丢给消息队列
                        rabbitTemplate.convertAndSend("retry.direct", "reUpdate.retry", newBlog);
                    }
                };
                EsRetry(retry);
            }

        }

    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        //TODO:验证文章是不是用户的
        removeById(id);
        tagBlogMapper.delete(Wrappers.lambdaQuery(TagBlog.class).eq(TagBlog::getBlogId,id));
        try {
            elasticsearchTemplate.delete(id.toString(), Blog.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            Runnable retry = () -> {
                int retryCount = 0;
                boolean success = false;
                while (retryCount < Max_Retry_Count) {
                    try {
                        elasticsearchTemplate.delete(id.toString(), Blog.class);
                        success = true;
                        break;
                    } catch (Exception ex) {
                        log.error("第:{}重试失败", retryCount + 1);
                    } finally {
                        retryCount++;
                    }
                    try {
                        Thread.sleep(Retry_Interval);
                    } catch (InterruptedException ex) {
                        log.error("重试被尝试打断");
                    }
                }
                if (!success) {
                    //本地重试失败,丢给消息队列
                    rabbitTemplate.convertAndSend("retry.direct", "reDelete.retry", id.toString());
                }
            };
            EsRetry(retry);
        }
        deleteCache(id.toString());
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


    private final AsyncTaskExecutor taskExecutor;

    @Override
    public BlogVO getBlogById(Long id, User user, boolean isDetail) {
        Blog base = blogCache.getIfPresent(id.toString());
        //Caffeine缓存未命中
        if(base == null) {
            base=(Blog)redisTemplate.opsForValue().get(id.toString());
            //redis缓存未命中
            if(base==null){
                base = getById(id);
            }
            if(base==null){
                throw new BizException(MessageConstant.UNKNOWN_BLOG);
            }
            blogCache.put(id.toString(), base);
            redisTemplate.opsForValue().set(id.toString(), base);
        }

        BlogVO blogVO=BlogVO.convertToVO(base);

        if(user != null){
            InteractionStatus status = interactionStatusMapper.getStatus(user.getId(), id);
            blogVO.setIsCollected(status.isCollected());
            blogVO.setIsLiked(status.isLiked());

            if (isDetail) {

                taskExecutor.execute(() -> {
                    addReadingRecord(id, user.getId());
                    //this.updateById(base);
                });

                //用set存读过该篇文章的用户的id,防止刷被阅读数
                redisTemplate.opsForSet().add("blog:be_read:"+base.getId(),user.getId());
            }

        }
        taskExecutor.execute(() -> statisticsService.addStatistics(user));


        return blogVO;
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



    public boolean updateById(Blog blog){
        Long id = blog.getId();
        if(id==null){
            throw new BizException("修改失败:文章id获取失败");
        }
        //先更新数据库再删redis缓存然后删caffeine缓存
        boolean updated = super.updateById(blog);
        deleteCache(id.toString());
        return updated;
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
    public List<BlogVO> listByIds(List<Long> ids,User user) {
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

        List<BlogVO> list = ordered.stream().map(BlogVO::convertToVO).toList();

        if (user != null) {
            List<Long> likedIds = interactionStatusMapper.getLikedBlogIds(user.getId(), ids);
            List<Long> collectedIds = interactionStatusMapper.getCollectedBlogIds(user.getId(), ids);

            Set<Long> likedSet = new HashSet<>(likedIds);
            Set<Long> collectedSet = new HashSet<>(collectedIds);

            for (BlogVO blogVO : list) {
                blogVO.setIsLiked(likedSet.contains(blogVO.getId()));
                blogVO.setIsCollected(collectedSet.contains(blogVO.getId()));
            }
        }
        return list;
    }


}
