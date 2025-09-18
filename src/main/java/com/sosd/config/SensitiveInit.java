package com.sosd.config;

import cn.hutool.dfa.SensitiveUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sosd.domain.POJO.SensitiveWord;
import com.sosd.mapper.SensitiveWordsMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SensitiveInit {

    private final SensitiveWordsMapper sensitiveWordsMapper;

    @PostConstruct
    public void init(){
        LambdaQueryWrapper<SensitiveWord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SensitiveWord::getWord);
        List<SensitiveWord> sensitiveWords = sensitiveWordsMapper.selectList(queryWrapper);
        System.out.println("id:"+sensitiveWords.get(0).getId());
        List<String> list = sensitiveWords.stream().map(word -> {
            return word.getWord();
        }).toList();
        SensitiveUtil.init(list);
    }
}
