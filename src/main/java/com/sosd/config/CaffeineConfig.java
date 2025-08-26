package com.sosd.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sosd.domain.POJO.Blog;

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<String,Blog> blogCache() {
        return Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .initialCapacity(100)
            .maximumSize(1000)
            .build();
    }
}