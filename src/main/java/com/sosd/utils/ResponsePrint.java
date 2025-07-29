package com.sosd.utils;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sosd.domain.DTO.Result;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 用于使用 Servlet 的 Response 打印信息
 */
@Component
public class ResponsePrint {

    /**
     * 将结果转化为 JSON 字符串
     */
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 将结果类转换成 JSON 字符串并传给前端
     * @param response
     * @param result
     * @throws IOException
     */
    public void print(HttpServletResponse response,Result result) throws IOException{

        //设置字符集以及响应类型
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        //将结果对象转换为 JSON 字符串并输出
        PrintWriter writer = response.getWriter();
        writer.println(objectMapper.writeValueAsString(result));
        writer.flush();
    }
}
