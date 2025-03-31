package com.sosd.domain.POJO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 历史记录实体类
 * 使用 Lombok 组件自动生成getter和setter，无参构造函数，有参构造函数
 * @author 应国浩
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`reading_record`")
public class ReadingRecord {
    
    /**
     * 无意义的主键，仅区分不同历史记录
     * 使用 Mybatis Plus 内置的雪花算法生成id
     */
    @TableId(value = "`id`",type = IdType.ASSIGN_ID)
    private Long id;
}
