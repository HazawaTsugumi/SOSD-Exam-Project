package com.sosd.mapper;

import com.sosd.domain.POJO.BeRead;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosd.domain.POJO.Blog;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章表的映射类
 * 使用 Mybatis Plus 不用写SQL简化开发
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog>{

    // @Options(useGeneratedKeys = true)
    // @Insert("insert into blog (id,title,user_id,content,`like`,create_time,update_time,collect,tag,user,`read`,comment,abstract_content)" +
    //         "values (#{id},#{title},#{userId},#{content},#{like},#{createTime},#{updateTime},#{collect},#{tag},#{user},#{read},#{comment},#{abstractContent})")
    // public Long insert();

    // @Select({
    //     "<script>",
    //     "select id,title,abstract_content from blog where id in",
    //     "(select blog_id from tag_blog where tag_id in",
    //     "<foreach collection='tagIds' open='(' close=')' separator=',' item='id'>",
    //     "#{id}",
    //     "</foreach>",
    //     ")",
    //     "order by read desc",
    //     "</script>"
    // })

//    @Options(useGeneratedKeys = true)
//    @Insert("insert into blog (id,title,user_id,content,`like`,create_time,update_time,collect,user_view,page_view,tag,user,`read`,comment,abstract_content)" +
//            "values (#{id},#{title},#{userId},#{content},#{like},#{createTime},#{updateTime},#{collect},#{userView},#{pageView},#{tag},#{user},#{read},#{comment},#{abstractContent})")
//    public Long insert();


    List<Blog> selectPageByTag(List<Long> tagIds);


}
