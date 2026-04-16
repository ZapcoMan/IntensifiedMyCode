package com.example.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.enums.RoleEnum;
import com.example.service.AdminService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token工具类，提供Token生成和用户信息获取功能
 * 使用固定的服务端密钥签发 JWT，通过 Redis 黑名单机制处理改密码后旧 token 失效
 */
@Component
public class TokenUtils {

    @Resource
    private AdminService adminService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private RedisUtils redisUtils;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-days:1}")
    private int expirationDays;

    /**
     * 生成Token（使用固定的服务端密钥）
     * @param userId 用户ID
     * @param role 用户角色
     * @return 生成的Token字符串
     */
    public String createToken(String userId, String role) {
        String audience = userId + "-" + role;
        Date expiresAt = DateUtil.offsetDay(new Date(), expirationDays);
        
        // 使用固定的服务端密钥签名
        String token = JWT.create()
                .withAudience(audience)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(jwtSecret));

        // 将token存储到Redis中，用于登出时快速失效
        if (redisUtils != null) {
            String tokenKey = "token:user:" + userId + ":" + role;
            redisUtils.set(tokenKey, token, expirationDays, TimeUnit.DAYS);
        }

        return token;
    }

    /**
     * 获取当前登录的用户信息
     * @return 当前登录的Account对象，如果没有登录或Token无效，则返回null
     */
    public Account getCurrentUser() {
        // 获取当前请求的HttpServletRequest对象
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null; // 测试环境无 HTTP 上下文时直接返回 null，避免抛异常
        }
        HttpServletRequest request = attrs.getRequest();

        // 优先从请求头中获取Token，如果不存在则从请求参数中获取
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }

        if (StrUtil.isBlank(token)) {
            return null;
        }

        // 解析Token获取载荷数据，并根据角色类型调用对应的服务获取用户信息
        String audience = JWT.decode(token).getAudience().get(0);
        String[] split = audience.split("-");
        String userId = split[0];
        String role = split[1];

        // 从Redis中获取用户信息
        String cacheKey = "user:info:" + userId + ":" + role;
        Account account = redisUtils != null ? redisUtils.get(cacheKey) : null;

        if (account == null) {
            // 如果Redis中没有，则从数据库查询（使用枚举统一管理角色判断）
            if (RoleEnum.isAdminRole(role)) {
                account = adminService != null ? adminService.selectById(userId) : null;
            } else if (RoleEnum.isUserRole(role)) {
                account = userService != null ? userService.selectById(userId) : null;
            }

            if (account != null && redisUtils != null) {
                // 将用户信息缓存到Redis
                redisUtils.set(cacheKey, account, 30, TimeUnit.MINUTES);
            }
        }

        return account;
    }

    /**
     * 验证Token是否有效（检查是否在黑名单中）
     * @param userId 用户ID
     * @param role 用户角色
     * @param token 待验证的token
     * @return true 如果Token有效，false 如果Token在黑名单中
     */
    public boolean isTokenValid(String userId, String role, String token) {
        if (redisUtils == null || StrUtil.isBlank(token)) {
            return true; // Redis 不可用时不做额外校验
        }
        
        String tokenKey = "token:user:" + userId + ":" + role;
        String storedToken = redisUtils.get(tokenKey);
        
        // 如果 Redis 中没有该 token（已登出或被加入黑名单），则无效
        return storedToken != null && storedToken.equals(token);
    }

    /**
     * 移除用户Token（登出或改密码时调用，加入黑名单）
     * @param userId 用户ID
     * @param role 用户角色
     */
    public void removeToken(String userId, String role) {
        if (redisUtils != null) {
            String tokenKey = "token:user:" + userId + ":" + role;
            redisUtils.remove(tokenKey);

            String cacheKey = "user:info:" + userId + ":" + role;
            redisUtils.remove(cacheKey);
        }
    }
}
