package com.sosd.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;


@Slf4j
public class DateToTimestampConverter implements Converter<Date, Timestamp> {
    // 定义与 Elasticsearch 日期格式匹配的转换器
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public Timestamp convert(@NotNull Date source) {

        String date = FORMATTER.format(source);
        return Timestamp.valueOf(date);
    }
}
