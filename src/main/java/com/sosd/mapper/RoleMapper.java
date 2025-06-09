package com.sosd.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosd.domain.POJO.Role;

/**
 * 角色表的映射类
 * 使用 Mybatis Plus 不用写SQL简化开发
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role>{
    
}
