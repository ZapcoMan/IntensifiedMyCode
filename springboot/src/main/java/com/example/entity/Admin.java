package com.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Admin extends Account {
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String name;

    private String email;
    private String phone;

    private String token;


}