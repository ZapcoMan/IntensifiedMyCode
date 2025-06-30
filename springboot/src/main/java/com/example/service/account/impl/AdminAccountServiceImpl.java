package com.example.service.account.impl;

import com.example.entity.Account;
import com.example.enums.RoleEnum;
import com.example.mapper.AdminMapper;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AdminAccountServiceImpl implements AccountService {

    @Resource
    AdminMapper adminMapper;

    @Override
    public RoleEnum getRole() {
        return RoleEnum.ADMIN;
    }

    @Override
    public Account selectById(String id) {
        return adminMapper.selectById(id);
    }
}