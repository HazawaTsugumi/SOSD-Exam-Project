package com.sosd.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sosd.domain.DTO.Result;
import com.sosd.service.BasicDataService;
import com.sosd.service.StatisticsService;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/data")
public class DataController {

    @Autowired
    private BasicDataService basicDataService;

    @Autowired
    private StatisticsService statisticsService;
    
    @GetMapping("/basic")
    @PreAuthorize("hasAnyRole('superAdmin', 'admin')")
    public Result getBasicData() throws IOException {
        return Result.success(basicDataService.getBasicData());
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('superAdmin', 'admin')")
    public Result getStatistics(@RequestParam("from")  @DateTimeFormat(iso = ISO.DATE) LocalDate from,@RequestParam("to")  @DateTimeFormat(iso = ISO.DATE) LocalDate to){
        return Result.success(statisticsService.getStatistics(Date.valueOf(from), Date.valueOf(to)));
    }
}
