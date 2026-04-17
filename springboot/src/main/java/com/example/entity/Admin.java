package com.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Admin extends Account {
    // 邮箱
    private String email;
    
    // 电话
    private String phone;
    
    // 状态
    private String status;
}