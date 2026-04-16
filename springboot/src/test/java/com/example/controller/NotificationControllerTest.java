package com.example.controller;

import com.example.TestBase;
import com.example.entity.Account;
import com.example.entity.Notification;
import com.example.service.NotificationService;
import com.example.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NotificationController 单元测试类
 * 测试通知相关的REST API接口
 */
@DisplayName("NotificationController 单元测试")
class NotificationControllerTest extends TestBase {

    @Mock
    private NotificationService notificationService;

    @Mock
    private TokenUtils tokenUtils;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private Notification testNotification;
    private Account mockAdmin;

    @BeforeEach
    void setUp() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();

        // 初始化测试数据
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(100L);
        testNotification.setTitle("系统通知");
        testNotification.setContent("这是一条测试通知");
        testNotification.setType("INFO");
        testNotification.setStatus("UNREAD");
        testNotification.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        // 初始化管理员账户
        mockAdmin = new Account();
        mockAdmin.setId(1);
        mockAdmin.setUsername("admin");
        mockAdmin.setRole("SUPER_ADMIN");
    }

    @Test
    @DisplayName("发送通知 - 成功（管理员）")
    void testSendNotification_Success() throws Exception {
        // Given
        when(tokenUtils.getCurrentUser()).thenReturn(mockAdmin);
        doNothing().when(notificationService).sendNotification(any(Notification.class));
    
        // When & Then
        mockMvc.perform(post("/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":100,\"title\":\"新通知\",\"content\":\"通知内容\",\"type\":\"INFO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));
    
        verify(notificationService, times(1)).sendNotification(any(Notification.class));
    }
    
    @Test
    @DisplayName("发送通知 - 失败（非管理员）")
    void testSendNotification_Forbidden() throws Exception {
        // Given
        Account regularUser = new Account();
        regularUser.setId(2);
        regularUser.setUsername("user");
        regularUser.setRole("USER");
        when(tokenUtils.getCurrentUser()).thenReturn(regularUser);
    
        // When & Then
        mockMvc.perform(post("/notification/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":100,\"title\":\"新通知\",\"content\":\"通知内容\",\"type\":\"INFO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    
        verify(notificationService, never()).sendNotification(any(Notification.class));
    }

    @Test
    @DisplayName("获取用户通知 - 成功")
    void testGetUserNotifications_Success() throws Exception {
        // Given
        Long userId = 100L;
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationService.getUserNotifications(userId)).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/notification/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(notificationService, times(1)).getUserNotifications(userId);
    }

    @Test
    @DisplayName("标记通知为已读 - 成功")
    void testMarkAsRead_Success() throws Exception {
        // Given
        Long notificationId = 1L;
        doNothing().when(notificationService).markAsRead(notificationId);

        // When & Then
        mockMvc.perform(post("/notification/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(notificationService, times(1)).markAsRead(notificationId);
    }

    @Test
    @DisplayName("删除通知 - 成功")
    void testDeleteNotification_Success() throws Exception {
        // Given
        Long notificationId = 1L;
        doNothing().when(notificationService).deleteNotification(notificationId);

        // When & Then
        mockMvc.perform(delete("/notification/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(notificationService, times(1)).deleteNotification(notificationId);
    }
}
