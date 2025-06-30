package com.example.service;

import com.example.entity.Account;
import com.example.enums.RoleEnum;

public interface AccountService {
    RoleEnum getRole();  // 标明服务支持哪个角色
    Account selectById(String id);
}