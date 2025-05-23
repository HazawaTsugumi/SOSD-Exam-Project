package com.sosd.project;

import com.sosd.domain.POJO.Blog;
import com.sosd.mapper.BlogMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;

@SpringBootTest
class ProjectApplicationTests {
	@Autowired
	ElasticsearchTemplate elasticsearchTemplate;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private BlogMapper blogMapper;

	@Test
	void contextLoads() {
		System.out.println(passwordEncoder.encode("222316"));
	}

	@Test
	void getAllIndex() throws IOException {
		GetIndexRequest request = new GetIndexRequest("*");
		RestHighLevelClient client=new RestHighLevelClient(RestClient.builder(new HttpHost("localhost",9200,"http")));
		GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
		System.out.println(Arrays.toString(getIndexResponse.getIndices()));
		client.close();
	}
	@Test
	void returnId(){
		Blog blog=new Blog();
		blog.setUserId(1L);
		blog.setTag("aa");
		blog.setTitle("aa");
		blog.setContent("aa");
		blog.setLike(0L);
		blog.setCreateTime(new Timestamp(System.currentTimeMillis()));
		blog.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		blog.setCollect(0L);
		blog.setUserView(0L);
		blog.setPageView(0L);
		blog.setRead(0L);
		blog.setComment(0L);
		blog.setUser("aa");
		blogMapper.insert(blog);
		System.out.println(blog.getId());
	}
}
