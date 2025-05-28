package com.sosd.config;

import io.minio.MinioClient;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "my.minio.credentials")
@Data
public class MinIOConfig {
    private String endpoint;
    private String account;
    private String password;
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(endpoint).credentials(account, password).build();
    }
}
