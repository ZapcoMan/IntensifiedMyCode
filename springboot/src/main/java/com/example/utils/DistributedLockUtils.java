package com.example.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class DistributedLockUtils {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final int DEFAULT_EXPIRE_TIME = 30; // 默认过期时间30秒

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁的键
     * @param requestId 请求ID，用于解锁时验证
     * @param expireTime 锁的过期时间（秒）
     * @return 是否获取锁成功
     */
    public boolean tryLock(String lockKey, String requestId, int expireTime) {
        String key = LOCK_PREFIX + lockKey;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, requestId, expireTime, TimeUnit.SECONDS);
        return result != null && result;
    }

    /**
     * 尝试获取分布式锁（使用默认过期时间）
     *
     * @param lockKey 锁的键
     * @param requestId 请求ID，用于解锁时验证
     * @return 是否获取锁成功
     */
    public boolean tryLock(String lockKey, String requestId) {
        return tryLock(lockKey, requestId, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 释放分布式锁（使用 Lua 脚本保证原子性）
     *
     * @param lockKey 锁的键
     * @param requestId 请求ID，用于验证是否是锁的持有者
     * @return 是否释放锁成功
     */
    public boolean releaseLock(String lockKey, String requestId) {
        String key = LOCK_PREFIX + lockKey;
        
        // Lua 脚本：先比较 value，相等才删除，保证原子性
        String luaScript = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end";
        
        Long result = redisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Long>) connection -> 
                connection.eval(
                    luaScript.getBytes(),
                    org.springframework.data.redis.connection.ReturnType.INTEGER,
                    1,
                    key.getBytes(),
                    requestId.getBytes()
                )
        );
        
        return result != null && result > 0;
    }
}