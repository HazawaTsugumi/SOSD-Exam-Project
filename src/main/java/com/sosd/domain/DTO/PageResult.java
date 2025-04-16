package com.sosd.domain.DTO;

import lombok.Data;

import java.util.List;

@Data
public class PageResult {
    private long total;
    private List<?> rows;
}
