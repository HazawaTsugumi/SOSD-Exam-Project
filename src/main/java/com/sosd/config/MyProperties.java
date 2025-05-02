package com.sosd.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "my")
@Data
public class MyProperties {
    private String secret;
    private String adminEmail;
    private List<String> whitelist;
}
