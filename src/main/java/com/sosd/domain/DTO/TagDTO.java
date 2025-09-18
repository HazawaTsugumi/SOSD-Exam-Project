package com.sosd.domain.DTO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
        /**
         * 标签id
         * 使用 Mybatis Plus 内置的雪花算法自动生成
         */
        @TableId(value = "`id`",type = IdType.ASSIGN_ID)
        private Long id;

        /**
         * 标签名称
         */
        @TableField("`name`")
        private String name;
}
