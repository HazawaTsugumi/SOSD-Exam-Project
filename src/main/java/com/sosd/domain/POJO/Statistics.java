package com.sosd.domain.POJO;

import java.sql.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`statistics`")
public class Statistics {
    
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("`date`")
    private Date date;

    @TableField("`pv`")
    private Long pv;

    @TableField("`uv`")
    private Long uv;
}
