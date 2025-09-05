package com.sosd.domain.DTO;

import com.sosd.domain.POJO.Tag;
import lombok.Data;

import java.util.List;

@Data
public class BlogDTO {
    private String title;
    private String content;
    private List<Tag> tags;
}
