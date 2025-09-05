package com.sosd.domain.query;

import com.sosd.domain.POJO.Blog;
import com.sosd.domain.POJO.Tag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BlogsQuery extends PageQuery {
    private List<Tag> tags;

}
