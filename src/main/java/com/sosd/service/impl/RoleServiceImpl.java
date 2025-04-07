package com.sosd.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sosd.domain.POJO.Role;
import com.sosd.mapper.RoleMapper;
import com.sosd.service.RoleService;

/**
 * 角色服务接口对应的实现类
 * @author 应国浩
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper,Role> implements RoleService{
    
}
