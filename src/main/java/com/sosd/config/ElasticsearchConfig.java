package com.sosd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class ElasticsearchConfig extends ElasticsearchConfigurationSupport {

    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        log.info("DateTimeToTimestampConverter registered");
        // 定义转换器列表（包含 Date → Timestamp）
        List<?> converters = List.of(
                new DateToTimestampConverter() // 你的自定义转换器
        );
        return new ElasticsearchCustomConversions(converters);
    }
}
