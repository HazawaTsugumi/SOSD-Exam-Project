package com.sosd.Exception;

/**
 * 自定义异常，之后若遇到业务错误，如用户不存在等问题抛出此异常
 * @author 应国浩
 */
public class BizException extends RuntimeException{
    
    /**
     * 只是一个壳而已，保证错误一定要有消息
     * @param message 错误信息
     */
    public BizException(String message) {
        super(message);
    }
}
