package com.sosd.controller;

import cn.hutool.dfa.SensitiveUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.DTO.Result;
import com.sosd.domain.POJO.SensitiveWord;
import com.sosd.domain.query.PageQuery;
import com.sosd.service.SensitiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@RestController
@Slf4j
@RequestMapping("/sensitive")
@RequiredArgsConstructor
public class SensitiveWordsController {

    private final SensitiveService sensitiveService;

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @GetMapping("/all")
    public Result getAll(){
        return Result.success(sensitiveService.list().stream().map(SensitiveWord::getWord).toList());
    }

    @GetMapping("/page")
    public Result page(PageQuery pageQuery) {
        Page<SensitiveWord> page=new Page<>(pageQuery.getPageNo(),pageQuery.getPageSize());
        Page<SensitiveWord> pageResult = sensitiveService.page(page);
        return Result.success(new PageResult(pageResult.getTotal()
                ,pageResult.getRecords().stream().map(SensitiveWord::getWord).toList()));
    }

    @DeleteMapping
    public Result delete(Long id){
        sensitiveService.removeById(id);
        lock.writeLock().lock();
        try {
            SensitiveUtil.init((List<String>)getAll().getData());
        } finally {
            lock.writeLock().unlock();
        }
        return Result.success(null);
    }

    @PostMapping
    @Transactional
    public Result insert(String words){
        String[] wordArr=words.split(",");
        List<String> list = Arrays.stream(wordArr).toList();
        //先插入库
        boolean success = sensitiveService.saveBatch(list.stream().map(word -> {
            return new SensitiveWord(word);
        }).toList());
        //写锁
        lock.writeLock().lock();
        try {
            if(success){
                SensitiveUtil.init((List<String>)getAll().getData());
            }
        } finally {
            lock.writeLock().unlock();
        }
        return Result.success(null);
    }

    public boolean judge(String sentence){
        //多线程读,单/少 线程新增敏感词
        lock.readLock().lock();
        try {
            return SensitiveUtil.containsSensitive(sentence);
        } finally {
            lock.readLock().unlock();
        }
    }

}
