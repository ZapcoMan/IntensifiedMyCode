package com.example.service.impl;


import com.example.entity.Account;
import com.example.service.QrCodeLoginService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class QrCodeLoginServiceImpl implements QrCodeLoginService {

    private static final String PREFIX = "qr:login:";
    private static final int EXPIRE_SECONDS = 300;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String generateToken() {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, "", EXPIRE_SECONDS, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public Account getAccountIfConfirmed(String token) {
        String key = PREFIX + token;
        String value = redisTemplate.opsForValue().get(key);
        if (value != null && !value.isEmpty()) {
            try {
                return objectMapper.readValue(value, Account.class);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void confirmLogin(String token, Account account) {
        try {
            String json = objectMapper.writeValueAsString(account);
            redisTemplate.opsForValue().set(PREFIX + token, json, EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("序列化失败");
        }
    }
}

