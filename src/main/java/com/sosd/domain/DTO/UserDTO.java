package com.sosd.domain.DTO;

import lombok.Data;

/**
 * 用于接收前端注册使用的实体类
 */
@Data
public class UserDTO {
    
    private String username;

    private String password;

    private String email;

    private String code;

    private String name;
}
