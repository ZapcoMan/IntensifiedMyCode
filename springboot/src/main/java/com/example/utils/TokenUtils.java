package com.example.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.entity.Account;
import com.example.service.impl.AdminServiceImpl;
import com.example.service.impl.UserServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * Token工具类，提供Token生成和用户信息获取功能
 */
@Component
public class TokenUtils {

    // 注入AdminServiceImpl和UserServiceImpl用于获取用户信息
    @Resource
    AdminServiceImpl adminServiceImpl;
    @Resource
    UserServiceImpl userServiceImpl;
    @Resource
    RedisUtils redisUtils;

    // 静态变量用于存储Service实例，以便在静态方法中使用
    static AdminServiceImpl staticAdminServiceImpl;;
    static UserServiceImpl staticUserServiceImpl;
    static RedisUtils staticRedisUtils;

    /**
     * 在Spring Boot工程启动后初始化静态变量
     */
    @PostConstruct
    public void init() {
        staticAdminServiceImpl = adminServiceImpl;
        staticUserServiceImpl = userServiceImpl;
        staticRedisUtils = redisUtils;
    }

    /**
     * 生成Token
     * @param data Token中的载荷数据，这里包含用户ID和角色，用"-"分隔
     * @param sign Token的密钥，用于签名
     * @return 生成的Token字符串
     */
    public static String createToken(String data, String sign) {
        String token = JWT.create().withAudience(data) // 将 userId-role 保存到 token 里面,作为载荷
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1)) // 1天后token过期
                .sign(Algorithm.HMAC256(sign)); // 以 password 作为 token 的密钥, HMAC256算法加密

        // 将token存储到Redis中，用于后续验证
        String[] split = data.split("-");
        String userId = split[0];
        String role = split[1];
        String tokenKey = "token:user:" + userId + ":" + role;
        staticRedisUtils.set(tokenKey, token, 1, java.util.concurrent.TimeUnit.DAYS);

        return token;
    }

    /**
     * 获取当前登录的用户信息
     * @return 当前登录的Account对象，如果没有登录或Token无效，则返回null
     */
    public static Account getCurrentUser() {
        // 获取当前请求的HttpServletRequest对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // 优先从请求头中获取Token，如果不存在，则从请求参数中获取
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
        Account account = staticRedisUtils.get(cacheKey);

        if (account == null) {
            // 如果Redis中没有，则从数据库查询
            if ("SUPER_ADMIN".equals(role)) {
                account = staticAdminServiceImpl.selectById(userId);
            } else if ("USER".equals(role)) {
                account = staticUserServiceImpl.selectById(userId);
            }

            if (account != null) {
                // 将用户信息缓存到Redis
                staticRedisUtils.set(cacheKey, account, 30, java.util.concurrent.TimeUnit.MINUTES);
            }
        }

        return account;
    }

    /**
     * 验证Token是否有效
     * @return true 如果Token有效，false 如果Token无效或不存在
     */
    public static boolean validateToken() {
        try {
            Account currentUser = getCurrentUser();
            return currentUser != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 移除用户Token（登出时调用）
     * @param userId 用户ID
     * @param role 用户角色
     */
    public static void removeToken(String userId, String role) {
        String tokenKey = "token:user:" + userId + ":" + role;
        staticRedisUtils.remove(tokenKey);

        String cacheKey = "user:info:" + userId + ":" + role;
        staticRedisUtils.remove(cacheKey);
    }
}