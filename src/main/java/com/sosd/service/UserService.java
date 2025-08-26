package com.sosd.service;

import java.io.IOException;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sosd.domain.DTO.PageResult;
import com.sosd.domain.POJO.User;

/**
 * 用户服务类的接口
 * 使用 Mybatis Plus 自动生成对应crud操作的接口
 */
public interface UserService extends IService<User>{
    
    /**
     * 处理用户登录操作
     * @param user
     */
    public void register(User user,String code) throws IOException;

    /**
     * 处理重置密码的操作
     * @param user
     * @param code
     */
    public void forgetPassword(User user,String code);

    /**
     * 根据用户id获取用户信息
     * @param id
     * @return
     */
    public User getUserInfoById(Integer id);

    /**
     * 修改用户昵称
     * @param id
     * @param nickName
     */
    public void updateNickname(Long id, String nickName);

    /**
     * 分页获取所有用户信息
     * @param page 页码
     * @param size 每页大小
     * @return 用户信息列表
     */
    public PageResult getAllUser(Integer page, Integer size);

    /**
     * 修改指定用户的权限
     * @param user
     */
    public void authUser(User user);
}
