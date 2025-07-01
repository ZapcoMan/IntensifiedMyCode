package com.example.service;

import com.example.entity.Account;

public interface QrCodeLoginService {
    String generateToken();
    Account getAccountIfConfirmed(String token);
    void confirmLogin(String token, Account account);
}