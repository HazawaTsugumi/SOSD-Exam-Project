package com.sosd.domain.DTO;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
//分页结果
public class PageDTO<T> {
    //总条数
    private Long total;
    //总页数
    private Long pages;

    private List<T> list;

    //使用默认的属性拷贝将PO转换成VO
    public static <VO,PO> PageDTO<VO> of (Page<PO> page, Class<VO> dtoClass) {
        PageDTO<VO> pageDTO=new PageDTO<VO>();
        pageDTO.setTotal(page.getTotal());
        pageDTO.setPages(page.getPages());
        List<PO> records = page.getRecords();
        if(records!=null && !records.isEmpty()){
            List<VO> list = new ArrayList<>();
            records.forEach(record->{
                try {
                    VO VO = dtoClass.getDeclaredConstructor().newInstance();
                    BeanUtils.copyProperties(record, VO);
                    list.add(VO);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            pageDTO.setList(list);
        }else{
            pageDTO.setList(new ArrayList<VO>());
        }
        return pageDTO;
    }
    //使用自定义转换器将PO转换成VO
    public static <VO,PO> PageDTO<VO> of (Page<PO> page, Function<PO, VO> convertor) {
        PageDTO<VO> pageDTO=new PageDTO<VO>();
        pageDTO.setTotal(page.getTotal());
        pageDTO.setPages(page.getPages());
        List<PO> records = page.getRecords();
        if(records!=null && !records.isEmpty()){
            pageDTO.setList(records.stream().map(convertor).toList());
        }else{
            pageDTO.setList(new ArrayList<VO>());
        }
        return pageDTO;
    }

    public static <VO> PageDTO<VO> empty(){
        PageDTO<VO> pageDTO=new PageDTO<>();
        pageDTO.setTotal(0L);
        pageDTO.setPages(0L);
        pageDTO.setList(Collections.emptyList());
        return pageDTO;
    }
}
