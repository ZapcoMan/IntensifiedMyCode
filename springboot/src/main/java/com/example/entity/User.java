package com.example.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class User extends Account {
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String name;

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    private String token;

    private String avatar;





    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}