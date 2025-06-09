package com.sosd.domain.DTO;

import lombok.Getter;

/**
 * 最终返回给前端的对象
 */
@Getter
public class Result {
    
    /**
     * 响应代码，200表示成功，其他表示失败
     */
    private Integer code;

    /**
     * 信息，如果成功为 success 失败则必须指定内容
     */
    private String message;

    /**
     * 数据，如果成功返回所需数据，失败为null
     */
    private Object data;

    /**
     * 使用静态方法配置请求成功的响应数据
     * @param data
     * @return
     */
    public static Result success(Object data){
        Result result = new Result();
        result.code = 200;
        result.message = "success";
        result.data = data;
        return result;
    }

    /**
     * 使用静态方法配置请求失败的响应数据
     * @param msg
     * @param code
     * @return
     */
    public static Result fail(String msg,Integer code){
        Result result = new Result();
        result.code = code;
        result.message = msg;
        result.data = null;
        return result;
    }

    /**
     * 使用重载静态方法配置请求失败的默认响应数据，即代码为-1的数据
     * @param msg
     * @return
     */
    public static Result fail(String msg){
        return Result.fail(msg, -1);
    }
}
