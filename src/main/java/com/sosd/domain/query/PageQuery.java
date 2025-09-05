package com.sosd.domain.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
//分页查询实体
public class PageQuery {
    private Long pageNo = 0L;

    private Long pageSize = 5L;

    private String sortBy;

    private boolean asc = true;

    public <T> Page<T> toMPPage (OrderItem ... items) {
        Page<T> page = Page.of(pageNo, pageSize);

        if(sortBy!=null && !sortBy.isEmpty() && !sortBy.isBlank()){
            page.addOrder(new OrderItem().setColumn(sortBy).setAsc(asc));
        }else if(items!=null){
            page.addOrder(items);
        }
        return page;
    }

    public <T> Page<T> toMPPageDefaultSortOfUpdateTime () {
        return toMPPage(new OrderItem().setColumn("update_time").setAsc(asc));
    }

    public <T> Page<T> toMPPageDefaultSortOfCreateTime () {
        return toMPPage(new OrderItem().setColumn("create_time").setAsc(asc));
    }
}
