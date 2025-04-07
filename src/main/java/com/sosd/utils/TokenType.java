package com.sosd.utils;

/**
 * 用于 token 类型的枚举类，保证生成的 token 只由如下两类的其中之一
 */
public enum TokenType {

    /**
     * 表示生成的 Token 是 Access 类型，其有效期为2个小时
     */
    ACCESS((long) (1000 * 60 * 60 * 2)),

    /**
     * 表示生成的 Token 是 Refresh 类型，其有效期为2天
     */
    REFRESH((long) (1000 * 60 * 60 * 24 * 2));

    private final Long time;

    TokenType(Long time) {
        this.time = time;
    }

    public Long getTime() {
        return time;
    }
}
