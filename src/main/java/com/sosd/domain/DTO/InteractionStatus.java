package com.sosd.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractionStatus {
    
    private boolean liked;

    private boolean collected;

    private Long blogId;
}
