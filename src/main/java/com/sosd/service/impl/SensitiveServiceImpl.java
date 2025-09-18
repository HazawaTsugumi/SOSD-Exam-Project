package com.sosd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.domain.POJO.SensitiveWord;
import com.sosd.mapper.SensitiveWordsMapper;
import com.sosd.service.SensitiveService;
import org.springframework.stereotype.Service;

@Service
public class SensitiveServiceImpl extends ServiceImpl<SensitiveWordsMapper, SensitiveWord> implements SensitiveService {
}
