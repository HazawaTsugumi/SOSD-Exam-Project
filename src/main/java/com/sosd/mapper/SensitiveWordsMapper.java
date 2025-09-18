package com.sosd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sosd.domain.POJO.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SensitiveWordsMapper extends BaseMapper<SensitiveWord> {
}
