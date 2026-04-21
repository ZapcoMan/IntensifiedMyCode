package com.example.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 基于 Bucket4j + Redis 实现分布式限流
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流key的前缀
     */
    String keyPrefix() default "";
    
    /**
     * 限流维度：USER（按用户）、IP（按IP地址）
     */
    LimitType limitType() default LimitType.USER;
    
    /**
     * 令牌桶容量（最大请求数）
     */
    int capacity() default 100;
    
    /**
     * 令牌填充速率（每秒填充的令牌数）
     */
    double refillRate() default 100;
    
    /**
     * 时间窗口（秒）
     */
    int timeWindow() default 60;
    
    /**
     * 限流后的提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
    
    /**
     * 限流类型枚举
     */
    enum LimitType {
        /** 按用户ID限流 */
        USER,
        /** 按IP地址限流 */
        IP
    }
}
