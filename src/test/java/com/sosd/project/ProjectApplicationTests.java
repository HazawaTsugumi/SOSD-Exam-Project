package com.sosd.project;

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
import java.util.Arrays;

@SpringBootTest
class ProjectApplicationTests {
	@Autowired
	ElasticsearchTemplate elasticsearchTemplate;
	@Autowired
	private PasswordEncoder passwordEncoder;

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
}
