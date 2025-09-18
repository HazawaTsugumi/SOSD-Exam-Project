package com.sosd.config;

import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

/**
 * 配置 Mybatis Plus 的配置类
 */
@Configuration
public class MpConfig {
    
    /**
     * 初始化 Mybatis Plus 的插件 Bean
     * @return 构造完成的插件类
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        //配置 Mybatis Plus 分页助手插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        //配置乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }


}
