package com.example.service.account.impl;

import com.example.entity.Account;
import com.example.enums.RoleEnum;
import com.example.mapper.UserMapper;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserAccountServiceImpl implements AccountService {

    @Resource
    UserMapper userMapper;

    @Override
    public RoleEnum getRole() {
        return RoleEnum.STUDENT;
    }

    @Override
    public Account selectById(String id) {
        return userMapper.selectById(id);
    }
}