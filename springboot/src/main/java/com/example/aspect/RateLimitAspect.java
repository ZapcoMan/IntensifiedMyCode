package com.example.aspect;

import com.example.annotation.RateLimit;
import com.example.common.R;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * 接口限流切面
 * 基于 Bucket4j + Redis 实现分布式限流
 */
@Aspect
@Component
public class RateLimitAspect {

    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;

    private ProxyManager<byte[]> proxyManager;
    private RedisClient redisClient;

    /**
     * 初始化 Bucket4j 代理管理器
     */
    private synchronized ProxyManager<byte[]> getProxyManager() {
        if (proxyManager == null) {
            // 创建独立的 Redis 客户端用于 Bucket4j
            String host = lettuceConnectionFactory.getHostName();
            int port = lettuceConnectionFactory.getPort();
            this.redisClient = RedisClient.create("redis://" + host + ":" + port);
            
            this.proxyManager = LettuceBasedProxyManager.builderFor(redisClient).build();
        }
        return proxyManager;
    }

    /**
     * 限流环绕通知
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 获取限流key
        String key = generateKey(rateLimit);
        
        // 获取或创建令牌桶
        Bucket bucket = getOrCreateBucket(key, rateLimit);
        
        // 尝试获取令牌
        if (bucket.tryConsume(1)) {
            // 有令牌，放行请求
            return joinPoint.proceed();
        } else {
            // 无令牌，返回限流错误
            return R.error(429, rateLimit.message());
        }
    }

    /**
     * 生成限流key
     */
    private String generateKey(RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder("rate_limit:");
        
        // 添加前缀
        if (!rateLimit.keyPrefix().isEmpty()) {
            keyBuilder.append(rateLimit.keyPrefix()).append(":");
        }
        
        // 根据限流类型添加标识
        if (rateLimit.limitType() == RateLimit.LimitType.IP) {
            keyBuilder.append("ip:").append(getIpAddress());
        } else {
            keyBuilder.append("user:").append(getCurrentUserId());
        }
        
        return keyBuilder.toString();
    }

    /**
     * 获取或创建令牌桶
     */
    private Bucket getOrCreateBucket(String key, RateLimit rateLimit) {
        ProxyManager<byte[]> proxyManager = getProxyManager();
        
        // 将 String key 转换为 byte[]
        byte[] keyBytes = key.getBytes();
        
        // 配置令牌桶 - 使用 Bucket4j 8.x 正确API
        Bandwidth bandwidth = Bandwidth.classic(
            rateLimit.capacity(),
            Refill.intervally((long) rateLimit.refillRate(), Duration.ofSeconds(rateLimit.timeWindow()))
        );
        
        BucketConfiguration configuration = BucketConfiguration.builder()
            .addLimit(bandwidth)
            .build();
        
        // 从 Redis 获取或创建 Bucket
        return proxyManager.builder().build(keyBytes, () -> configuration);
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String token = request.getHeader("token");
                if (token != null && !token.isEmpty()) {
                    // 使用token作为用户标识
                    return token;
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "anonymous";
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                // 取第一个IP（防止代理链）
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "unknown";
    }
}
