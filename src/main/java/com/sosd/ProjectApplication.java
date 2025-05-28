package com.sosd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot启动类
 * @author 应国浩
 * @version 1.1
 */
@SpringBootApplication
@MapperScan("com.sosd.mapper")
public class ProjectApplication {

	/**
	 * 一切的开始
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}
