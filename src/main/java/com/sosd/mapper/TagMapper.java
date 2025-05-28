package com.sosd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosd.domain.POJO.Tag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签表的映射类
 * 使用 Mybatis Plus 不用写SQL简化开发
 * @author 应国浩
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag>{
    @Insert("insert into tag (name) values(#{name})")
    public void insertTag(String name);
}
