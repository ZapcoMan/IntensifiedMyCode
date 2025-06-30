package com.example.service;

import com.example.entity.Account;
import com.example.entity.User;
import com.example.enums.RoleEnum;

public interface AccountService {
    RoleEnum getRole();  // 标明服务支持哪个角色
    Account selectById(String id);

    void updatePassword(Account account);
    Account login(Account account);
    default void register(User user) {
        throw new UnsupportedOperationException("该角色不支持注册");
    }
}