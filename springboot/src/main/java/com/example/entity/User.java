package com.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户信息类，继承自Account类
 * 该类主要用于表示系统中的用户，包含用户的详细信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends Account {
    // 邮箱
    private String email;

    // 电话
    private String phone;
}
