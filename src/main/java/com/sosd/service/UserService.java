package com.sosd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.POJO.User;

/**
 * 用户服务类的接口
 * 使用 Mybatis Plus 自动生成对应crud操作的接口
 * @author 应国浩
 */
public interface UserService extends IService<User>{
    
    /**
     * 处理用户登录操作
     * @param user
     */
    public void register(User user,String code);
}
