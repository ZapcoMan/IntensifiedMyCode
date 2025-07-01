package com.example.entity;

import lombok.Data;

@Data
public  class ConfirmDto {
    private String token;
    private Account account;

}