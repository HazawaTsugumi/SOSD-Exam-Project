package com.sosd.service;

import java.io.IOException;

import com.sosd.domain.DTO.BasicData;

public interface BasicDataService {
    
    public BasicData getBasicData() throws IOException;

    public void setBasicData(BasicData data) throws IOException;
}
