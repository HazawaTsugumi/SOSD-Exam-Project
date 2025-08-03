package com.sosd.domain.DTO;

import java.util.List;

import com.sosd.domain.POJO.Comment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentDTO {
    
    private Comment comment;

    private List<CommentDTO> children;
}
