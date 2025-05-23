package com.sosd.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://10.204.27.87:9000")
                .credentials("minioadmin","minioadmin")
                .build();
    }
}
