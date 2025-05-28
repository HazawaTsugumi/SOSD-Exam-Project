package com.sosd.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosd.domain.POJO.Blog;
import org.apache.ibatis.annotations.Options;

/**
 * 文章表的映射类
 * 使用 Mybatis Plus 不用写SQL简化开发
 * @author 应国浩
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog>{
    @Options(useGeneratedKeys = true)
    @Insert("insert into blog (id,title,user_id,content,`like`,create_time,update_time,collect,user_view,page_view,tag,user,`read`,comment,abstract_content)" +
            "values (#{id},#{title},#{userId},#{content},#{like},#{createTime},#{updateTime},#{collect},#{userView},#{pageView},#{tag},#{user},#{read},#{comment},#{abstractContent})")
    public Long insert();
}
