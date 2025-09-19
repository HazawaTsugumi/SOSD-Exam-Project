package com.sosd.project;


import cn.hutool.dfa.SensitiveUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.collection.sequence.SString;
import com.sosd.config.SensitiveInit;
import com.sosd.constant.MessageConstant;
import com.sosd.domain.POJO.BeRead;
import com.sosd.domain.POJO.Blog;
import com.sosd.domain.VO.BlogVO;
import com.sosd.mapper.BlogMapper;
import com.sosd.repository.BlogDao;
import com.sosd.service.BlogService;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

//TODO
@SpringBootTest()
class ProjectApplicationTests {
	@Autowired
	ElasticsearchTemplate elasticsearchTemplate;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private BlogService blogService;
    @Autowired
    private BlogDao blogDao;

	@Test
	void contextLoads() {
		System.out.println(passwordEncoder.encode("222316"));
	}

	@Test
	void getAllIndex() throws IOException {
//		GetIndexRequest request = new GetIndexRequest("*");
//		RestHighLevelClient client=new RestHighLevelClient(RestClient.builder(new HttpHost("localhost",9200,"http")));
//		GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
//		System.out.println(Arrays.toString(getIndexResponse.getIndices()));
//		client.close();
	}
	@Test
	void testAbstract(){

		List<String> strings = HanLP.extractSummary("SQLark是一款面向信创应用开发者的数据库开发和管理工具，可用于快速查询、创建和管理不同类型的数据库系统。SQLark具有如下特效：支持多种主流数据库：达梦数据库、Oracle、MySQL、PostgreSQL等。丰富的数据库对象支持：模式、表、视图、物化视图、函数、存储过程、序列、触发器等。SQL智能编辑器：基于SQL语法解析实现代码补全，提供精准的SQL编码提示。数据生成：快速生成千万级/亿级仿真表数据，支持用正则表达式设置规则。数据迁移：提供全生命周期的数据迁移解决方案，迁移过程包括迁移评估、迁移实施和迁移校验三大环节。ER图：能根据数据库/模式/表逆向ER图，帮助开发者快速理清数据库表之间的关系。小百灵AI：能提供代码生成、代码解释、报错分析、SQL优化等功能，帮助开发者高效完成数据库开发、数据分析及日常运维工作。", 10);
		System.out.println(strings);
	}
	@Test
	void testCommonMark(){
		String content="";
		Parser parser = Parser.builder().build();
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
		System.out.println(paragraphs);

	}

	@Autowired
	SensitiveInit sensitiveInit;
	@Test
	void test(){
//		WordTree tree = new WordTree();
//		tree.addWord("操你妈");
//		boolean bool = tree.isMatch("我的");
//		System.out.println(bool);
		boolean sens = SensitiveUtil.containsSensitive("草泥马");
		System.out.println(sens);
	}
	@Autowired
	private RedisTemplate redisTemplate;
	private static final int Size = 100;

	private static final Long Gap = 1000*60*60*24L;
	@Autowired
	private BlogMapper blogMapper;

	public static final String Last_Blog_Id="LastBlogId";

	public static final String Hot_Blogs="HotBlogs";

	public static final ObjectMapper objectMapper = new ObjectMapper();
	@Test
	public void test5(){
		List<Long> list = objectMapper.convertValue(redisTemplate.opsForValue().get(Hot_Blogs), new TypeReference<>() {
		});
		System.out.println(list);
	}
//	@Test
//	public void hotBlogsCalculation() {
//		Long lastBlogId = (Long)redisTemplate.opsForValue().get(Last_Blog_Id);
//		if(lastBlogId==null){
//			lastBlogId=0L;
//		}
//		List<Blog> newRecords;
//		{
//			LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
//			queryWrapper.select(Blog::getId,Blog::getRead).gt(Blog::getId, lastBlogId).orderByDesc(Blog::getId);
//			Page<Blog> page=new Page<>(0,Size);
//			newRecords = blogMapper.selectPage(page, queryWrapper).getRecords();
//		}
//		if(newRecords==null||newRecords.isEmpty()){
//			return;
//		}
//		redisTemplate.opsForValue().set(Last_Blog_Id,newRecords.get(0).getId());
//		List<Long> oldList = objectMapper
//				.convertValue(redisTemplate.opsForValue().get(Hot_Blog_Id), new TypeReference<>() {});
//		if(oldList ==null|| oldList.isEmpty()){
//			redisTemplate.opsForValue().set(Hot_Blog_Id,newRecords.stream().map(Blog::getId).toList());
//			return;
//		}
//		LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
//		List<Blog> oldRecords = blogMapper
//				.selectList(queryWrapper.select(Blog::getId,Blog::getRead).in(Blog::getId, oldList));
//		newRecords.addAll(oldRecords);
//		newRecords.sort(new Comparator<Blog>() {
//			@Override
//			public int compare(Blog o1, Blog o2) {
//				return o2.getRead().compareTo(o1.getRead());
//			}
//		});
//		redisTemplate.opsForValue().set(Hot_Blog_Id,newRecords.stream().limit(Size).map(Blog::getId).toList());
//	}
	@Test
	public void testBlogBatchUpdate(){
		List<BeRead> list=new ArrayList<>();
		list.add(new BeRead(1951847729727098882L,2));
		list.add(new BeRead(1951935008054763522L,3));
		for(int i=0;i< list.size();i++){
			blogService.update(Wrappers.lambdaUpdate(Blog.class)
					.setSql("`read` = `read` + "+list.get(i).getCount())
					.eq(Blog::getId, list.get(i).getId()));
		}
	}

	@Test
	public void testSearch(){
		Criteria criteria = new Criteria("title").matches("数据库");
		Query query=new CriteriaQuery(criteria);
		SearchHits<Blog> search = elasticsearchTemplate.search(query, Blog.class);
		System.out.println(search.getSearchHit(0).getContent().getTitle());
	}

	@Autowired
	private  Cache<Integer,Blog> hotBlogsCache;

	@Test
	public void hotBlogsCalculation() {
		int size = 100;

		PageRequest pageRequest = PageRequest
				.of(0, Size, Sort.by(Sort.Direction.DESC, "read"));
		NativeQuery nativeQuery = NativeQuery.builder().withPageable(pageRequest).build();
		SearchHits<Blog> search = elasticsearchTemplate.search(nativeQuery, Blog.class);
		List<Blog> list = search.getSearchHits().stream().map(SearchHit::getContent).toList();
		Set<ZSetOperations.TypedTuple<Blog>> set=new HashSet<>();
		for(int i=0;i<list.size();i++){
			ZSetOperations.TypedTuple<Blog> value= ZSetOperations.TypedTuple.of(list.get(i), (double) i);
			set.add(value);
			hotBlogsCache.put(i,list.get(i));
		}
		redisTemplate.opsForZSet().add(Hot_Blogs,set);
	}
	@Test
	public void testSearch1(){
		Criteria criteria=new Criteria("tag")
				.matches("数据库")
				.and("createTime")
				.greaterThan(Timestamp.valueOf(LocalDateTime.now().minusMonths(1)));
//				.lessThan(Timestamp.valueOf((LocalDateTime.now().minusMonths(1))));
		Query query=new CriteriaQuery(criteria);
		query.setPageable(PageRequest.of(0,5, Sort.by(Sort.Direction.DESC,"createTime")));
		SearchHits<Blog> search = elasticsearchTemplate.search(new CriteriaQuery(criteria), Blog.class);

	}
	@Test
	public void testZSet(){
		Set<Blog> range = redisTemplate.opsForZSet().range(Hot_Blogs, 0, -1);
		int init=0;
		if(range==null){
			throw new RuntimeException();
		}
		ArrayList<Blog> values=new ArrayList<>(range);
//		List<Blog> values = list.stream().map(ZSetOperations.TypedTuple::getValue).toList();
		for(int i=0;i<values.size();i++){
			System.out.println(values.get(i).getId());
		}
	}
	@Test
	public void testBlogVOConvert(){
		Blog blog=new Blog("哈哈哈哈");
		BlogVO blogVO = BlogVO.convertToVOForPage(blog);
		System.out.println(blogVO.getContent());
	}

	@Test
	public void checkFileSortRight(){
		String suffix="jpg";
		String maxHexString="FFD8FF11111";
		String magicNumberOfSuffix = MessageConstant.FILE_HEX_MAP.get(suffix);
		try {
			//若为空说明后缀名不符合要求
			Assert.hasLength(magicNumberOfSuffix,"");
		} catch (IllegalArgumentException e) {
			System.out.println(false);
		}

		System.out.println(maxHexString.matches(magicNumberOfSuffix+".*"));;
	}
}
