package com.sosd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot启动类
 * @version 1.1
 */
@SpringBootApplication(exclude = TaskExecutionAutoConfiguration.class)
@MapperScan("com.sosd.mapper")
@EnableScheduling
public class ProjectApplication {

	/**
	 * 一切的开始
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}
