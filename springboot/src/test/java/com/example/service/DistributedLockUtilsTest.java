package com.example.service;

import com.example.TestBase;
import com.example.utils.DistributedLockUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DistributedLockUtils 单元测试
 * 纯 Mockito 测试，不连 Redis，不启动 Spring 上下文
 */
class DistributedLockUtilsTest extends TestBase {

    private DistributedLockUtils lockUtils;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lockUtils = new DistributedLockUtils();
        // 注入 Mock 的 RedisTemplate（通过反射或 set 方法）
        var field = DistributedLockUtils.class.getDeclaredField("redisTemplate");
        field.setAccessible(true);
        field.set(lockUtils, redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void tryLock_成功获取锁() {
        // given: 锁未被占用
        when(valueOperations.setIfAbsent(eq("lock:testKey"), eq("req123"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        // when
        boolean result = lockUtils.tryLock("testKey", "req123", 10);

        // then
        assertTrue(result);
        verify(valueOperations).setIfAbsent("lock:testKey", "req123", 10L, TimeUnit.SECONDS);
    }

    @Test
    void tryLock_锁已被占用() {
        // given: 锁已被其他请求占用
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(false);

        // when
        boolean result = lockUtils.tryLock("testKey", "req123", 10);

        // then
        assertFalse(result);
    }

    @Test
    void tryLock_使用默认过期时间() {
        // given: 锁未被占用
        when(valueOperations.setIfAbsent(eq("lock:testKey"), eq("req123"), eq(30L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        // when: 不传过期时间，使用默认值 30 秒
        boolean result = lockUtils.tryLock("testKey", "req123");

        // then
        assertTrue(result);
        verify(valueOperations).setIfAbsent("lock:testKey", "req123", 30L, TimeUnit.SECONDS);
    }

    @Test
    void releaseLock_成功释放自己持有的锁() {
        // given: 当前请求持有锁
        when(valueOperations.get("lock:testKey")).thenReturn("req123");
        when(redisTemplate.delete("lock:testKey")).thenReturn(true);

        // when
        boolean result = lockUtils.releaseLock("testKey", "req123");

        // then
        assertTrue(result);
        verify(redisTemplate).delete("lock:testKey");
    }

    @Test
    void releaseLock_requestId不匹配不解锁() {
        // given: 锁被其他请求持有
        when(valueOperations.get("lock:testKey")).thenReturn("otherReq");

        // when: 用不同的 requestId 尝试解锁
        boolean result = lockUtils.releaseLock("testKey", "req123");

        // then: 解锁失败，锁键不应被删除
        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void releaseLock_锁已不存在() {
        // given: 锁已过期或不存在
        when(valueOperations.get("lock:testKey")).thenReturn(null);

        // when
        boolean result = lockUtils.releaseLock("testKey", "req123");

        // then
        assertFalse(result);
    }
}
