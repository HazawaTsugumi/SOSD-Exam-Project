package com.sosd.domain.DTO;

import lombok.Data;

import java.util.List;

@Data
public class PageResult {
    private Long total;
    private Long pages;
    private List<?> rows;
}
