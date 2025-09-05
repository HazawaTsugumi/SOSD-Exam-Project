package com.sosd.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.sosd.domain.DTO.InteractionStatus;

@Mapper
public interface InteractionStatusMapper {
    
    @Select("SELECT EXISTS(SELECT 1 FROM `like` WHERE user_id = #{userId} AND blog_id = #{blogId}) AS liked,EXISTS(SELECT 1 FROM `collect` WHERE user_id = #{userId} AND blog_id = #{blogId} ) AS collected")
    public InteractionStatus getStatus(@Param("userId") Long userId,@Param("blogId") Long blogId);

    @Select({
        "<script>",
        "SELECT blog_id FROM `like`",
        "WHERE user_id = #{userId} AND blog_id IN",
        "<foreach collection='blogIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<Long> getLikedBlogIds(@Param("userId") Long userId, @Param("blogIds") List<Long> blogIds);

    @Select({
        "<script>",
        "SELECT blog_id FROM `collect`",
        "WHERE user_id = #{userId} AND blog_id IN",
        "<foreach collection='blogIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<Long> getCollectedBlogIds(@Param("userId") Long userId, @Param("blogIds") List<Long> blogIds);
}
