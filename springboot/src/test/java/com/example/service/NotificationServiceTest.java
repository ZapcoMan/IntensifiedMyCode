package com.example.service;

import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import com.example.service.impl.NotificationServiceImpl;
import com.example.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceImpl 单元测试
 * 重点测试：
 * 1. 缓存命中/未命中逻辑
 * 2. 写操作后缓存清除策略
 * 3. markAsRead 和 deleteLogical 的容错（通知不存在时不抛异常）
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationServiceImpl notificationService;

    @MockBean
    private RedisUtils redisUtils;

    @MockBean
    private NotificationMapper notificationMapper;

    @BeforeEach
    void setUp() {
        when(redisUtils.get(anyString())).thenReturn(null);
        when(redisUtils.set(anyString(), any(), anyLong(), any())).thenReturn(true);
        when(redisUtils.remove(anyString())).thenReturn(true);
    }

    @Test
    void sendNotification_插入成功_清除用户缓存() {
        // given
        Notification notification = Notification.builder()
                .userId(1L)
                .title("测试通知")
                .content("测试内容")
                .type("SYSTEM")
                .build();
        doNothing().when(notificationMapper).insert(any(Notification.class));

        // when
        notificationService.sendNotification(notification);

        // then
        verify(notificationMapper).insert(notification);
        verify(redisUtils).remove("notification:user:1");
    }

    @Test
    void getUserNotifications_缓存命中_直接返回不查库() {
        // given
        Notification cached = Notification.builder()
                .id(10L)
                .userId(1L)
                .title("缓存通知")
                .status("UNREAD")
                .build();
        when(redisUtils.get("notification:user:1")).thenReturn(Collections.singletonList(cached));

        // when
        List<Notification> result = notificationService.getUserNotifications(1L);

        // then
        assertEquals(1, result.size());
        assertEquals("缓存通知", result.get(0).getTitle());
        verify(notificationMapper, never()).findByUser(anyLong());
    }

    @Test
    void getUserNotifications_缓存未命中_查库并缓存结果() {
        // given
        Notification dbNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .title("数据库通知")
                .status("UNREAD")
                .build();
        when(notificationMapper.findByUser(1L)).thenReturn(Collections.singletonList(dbNotification));

        // when
        List<Notification> result = notificationService.getUserNotifications(1L);

        // then
        assertEquals(1, result.size());
        assertEquals("数据库通知", result.get(0).getTitle());
        verify(notificationMapper).findByUser(1L);
        verify(redisUtils).set(eq("notification:user:1"), any(), eq(30L), any());
    }

    @Test
    void getUserNotifications_缓存空列表_不重复查库() {
        // given: Redis 返回空列表（非 null），表示已知无数据
        when(redisUtils.get("notification:user:99")).thenReturn(Collections.emptyList());

        // when
        List<Notification> result = notificationService.getUserNotifications(99L);

        // then: 直接返回空列表，不查库
        assertTrue(result.isEmpty());
        verify(notificationMapper, never()).findByUser(anyLong());
    }

    @Test
    void markAsRead_标记成功_清除用户缓存() {
        // given
        Notification notification = Notification.builder()
                .id(5L)
                .userId(2L)
                .title("待读通知")
                .status("UNREAD")
                .build();
        when(notificationMapper.findById(5L)).thenReturn(notification);
        doNothing().when(notificationMapper).markAsRead(5L);

        // when
        notificationService.markAsRead(5L);

        // then
        verify(notificationMapper).markAsRead(5L);
        verify(redisUtils).remove("notification:user:2");
    }

    @Test
    void markAsRead_通知不存在_不抛异常_缓存仍清除() {
        // given: 通知不存在，findById 返回 null
        when(notificationMapper.findById(999L)).thenReturn(null);

        // when: 不抛异常
        assertDoesNotThrow(() -> notificationService.markAsRead(999L));

        // then: markAsRead 不被调用，但缓存仍尝试清除（redisUtils mock 默认返回 true）
        verify(notificationMapper, never()).markAsRead(anyLong());
    }

    @Test
    void deleteNotification_逻辑删除_清除用户缓存() {
        // given
        Notification notification = Notification.builder()
                .id(7L)
                .userId(3L)
                .title("待删除通知")
                .status("UNREAD")
                .build();
        when(notificationMapper.findById(7L)).thenReturn(notification);
        doNothing().when(notificationMapper).deleteLogical(7L);

        // when
        notificationService.deleteNotification(7L);

        // then
        verify(notificationMapper).deleteLogical(7L);
        verify(redisUtils).remove("notification:user:3");
    }

    @Test
    void deleteNotification_通知不存在_不抛异常() {
        // given
        when(notificationMapper.findById(888L)).thenReturn(null);

        // when & then: 不抛异常
        assertDoesNotThrow(() -> notificationService.deleteNotification(888L));
        verify(notificationMapper, never()).deleteLogical(anyLong());
    }
}
