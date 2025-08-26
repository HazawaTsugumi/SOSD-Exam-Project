package com.sosd.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeStatus {
    
    private Boolean isLiked;

    private Boolean isCollected;
}
