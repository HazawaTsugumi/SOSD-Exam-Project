package com.sosd.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicData {
    
    private Long userCount;

    private Long blogCount;

    private Long likeCount;

    private Long commentCount;

    private Long collectCount;
}
