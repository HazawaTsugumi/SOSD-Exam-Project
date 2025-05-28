package com.sosd.project;

import com.hankcs.hanlp.HanLP;
import org.apache.http.HttpHost;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	@Test
	void testAbstract(){

		List<String> strings = HanLP.extractSummary("不知道你是否注意过，即便是在微博、都有流行的当下，网上还是有很多博客和博主，而且流量和粉丝都相当可观，有些博主还能通过接广告、吸引会员等方式来得到收入。你知道博客为什么这么流行且经久不衰吗？别急，下面就跟大家科普下一些关于博客的知识。", 1);
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
}
