package com.example.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类，使用 BCrypt 算法
 * BCrypt 优势：
 * 1. 自动加盐，防止彩虹表攻击
 * 2. 可调节计算复杂度，抵御暴力破解
 * 3. 每次加密结果不同，但验证结果一致
 */
@Component
public class PasswordEncoder {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码
     * @param rawPassword 明文密码
     * @return BCrypt 加密后的密码
     */
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
