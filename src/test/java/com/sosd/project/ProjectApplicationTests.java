package com.sosd.project;

import cn.hutool.db.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}
