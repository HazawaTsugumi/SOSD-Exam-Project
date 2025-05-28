package com.sosd.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.HanLP;
import com.sosd.Exception.BizException;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.DTO.BlogDTO;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.Tag;
import com.sosd.domain.POJO.User;
import com.sosd.domain.VO.BlogVO;
import com.sosd.mapper.BlogMapper;
import com.sosd.mapper.TagMapper;
import com.sosd.repository.BlogDao;
import com.sosd.service.BlogService;
import com.sosd.utils.JwtUtil;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.elasticsearch.search.aggregations.metrics.Min;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class BlogServiceImpl implements BlogService {
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
                blogVO.setContent(content);
            }else{
                blogVO.setContent(content.substring(0,50));
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
        blog.setUserView(0L);
        blog.setPageView(0L);
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
            HanLP.extractSummary(paragraphs.get(i),1);
            builder.append(paragraphs.get(i));
        }
        blog.setAbstractContent(builder.toString());

        try {
            int id = blogMapper.insert(blog);
            blog.setId((long) id);
            blogDao.save(blog);
        } catch (Exception e) {
            blogDao.deleteById(blog.getId());
            throw new BizException(MessageConstant.PUBLISH_ERROR);
        }
    }

    @Override
    public List<Tag> getTags() {
        return tagMapper.selectList(null);
    }

    @Override
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
                            .object(id +"."+suffix)
                            .stream(inputStream,image.length(),-1)
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
}
