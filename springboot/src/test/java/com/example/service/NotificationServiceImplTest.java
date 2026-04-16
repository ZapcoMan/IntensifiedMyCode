package com.example.service;

import com.example.TestBase;
import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import com.example.service.impl.NotificationServiceImpl;
import com.example.utils.RedisUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceImpl 单元测试类
 * 测试通知相关的业务逻辑
 */
@DisplayName("NotificationService 单元测试")
class NotificationServiceImplTest extends TestBase {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private RedisUtils redisUtils;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(100L);
        testNotification.setTitle("系统通知");
        testNotification.setContent("这是一条测试通知");
        testNotification.setType("INFO");
        testNotification.setStatus("UNREAD");
        testNotification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    @DisplayName("发送通知 - 成功")
    void testSendNotification_Success() {
        // Given
        Notification newNotification = new Notification();
        newNotification.setUserId(100L);
        newNotification.setTitle("新通知");
        newNotification.setContent("通知内容");
        newNotification.setType("INFO");

        // When
        notificationService.sendNotification(newNotification);

        // Then
        verify(notificationMapper, times(1)).insert(newNotification);
        verify(redisUtils, times(1)).remove("notification:user:100");
    }

    @Test
    @DisplayName("获取用户通知 - 成功（从缓存）")
    void testGetUserNotifications_FromCache() {
        // Given
        Long userId = 100L;
        List<Notification> cachedNotifications = Arrays.asList(testNotification);
        when(redisUtils.get("notification:user:" + userId)).thenReturn(cachedNotifications);

        // When
        List<Notification> result = notificationService.getUserNotifications(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisUtils, times(1)).get("notification:user:" + userId);
        verify(notificationMapper, never()).findByUser(anyLong());
    }

    @Test
    @DisplayName("获取用户通知 - 成功（从数据库）")
    void testGetUserNotifications_FromDatabase() {
        // Given
        Long userId = 100L;
        List<Notification> expectedNotifications = Arrays.asList(testNotification);
        when(redisUtils.get("notification:user:" + userId)).thenReturn(null);
        when(notificationMapper.findByUser(userId)).thenReturn(expectedNotifications);
        when(redisUtils.set(anyString(), anyList(), anyLong(), any())).thenReturn(true);

        // When
        List<Notification> result = notificationService.getUserNotifications(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisUtils, times(1)).get("notification:user:" + userId);
        verify(notificationMapper, times(1)).findByUser(userId);
    }

    @Test
    @DisplayName("标记通知为已读 - 成功")
    void testMarkAsRead_Success() {
        // Given
        Long notificationId = 1L;
        when(notificationMapper.findById(notificationId)).thenReturn(testNotification);

        // When
        notificationService.markAsRead(notificationId);

        // Then
        verify(notificationMapper, times(1)).markAsRead(notificationId);
        verify(notificationMapper, times(1)).findById(notificationId);
        verify(redisUtils, times(1)).remove("notification:user:100");
    }

    @Test
    @DisplayName("删除通知 - 成功")
    void testDeleteNotification_Success() {
        // Given
        Long notificationId = 1L;
        when(notificationMapper.findById(notificationId)).thenReturn(testNotification);

        // When
        notificationService.deleteNotification(notificationId);

        // Then
        verify(notificationMapper, times(1)).deleteLogical(notificationId);
        verify(notificationMapper, times(1)).findById(notificationId);
        verify(redisUtils, times(1)).remove("notification:user:100");
    }
}
