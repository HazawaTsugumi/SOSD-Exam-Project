package com.sosd.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosd.domain.POJO.User;

/**
 * 用户表的映射类
 * 使用 Mybatis Plus 不用写SQL简化开发
 * @author 应国浩
 */
@Mapper
public interface UserMapper extends BaseMapper<User>{
    
}
