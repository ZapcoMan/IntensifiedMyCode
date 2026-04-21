package com.example.controller;

import com.example.TestBase;
import com.example.entity.AuditLog;
import com.example.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuditLogController 单元测试类
 * 测试审计日志相关的REST API接口
 */
@DisplayName("AuditLogController 单元测试")
class AuditLogControllerTest extends TestBase {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogController auditLogController;

    private MockMvc mockMvc;
    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(auditLogController).build();

        // 初始化测试数据
        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setUsername("admin");
        testAuditLog.setAction("登录");
        testAuditLog.setResource("用户");
        testAuditLog.setIpAddress("127.0.0.1");
    }

    @Test
    @DisplayName("获取最近的审计日志 - 成功（默认参数）")
    void testGetRecentLogs_DefaultParams() throws Exception {
        // Given
        List<AuditLog> logs = Arrays.asList(testAuditLog);
        when(auditLogService.getRecentLogs(20)).thenReturn(logs);

        // When & Then
        mockMvc.perform(get("/api/audit/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(auditLogService, times(1)).getRecentLogs(20);
    }

    @Test
    @DisplayName("获取最近的审计日志 - 成功（自定义参数）")
    void testGetRecentLogs_CustomParams() throws Exception {
        // Given
        int limit = 50;
        List<AuditLog> logs = Arrays.asList(testAuditLog);
        when(auditLogService.getRecentLogs(limit)).thenReturn(logs);

        // When & Then
        mockMvc.perform(get("/api/audit/recent")
                .param("limit", String.valueOf(limit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(auditLogService, times(1)).getRecentLogs(limit);
    }

    @Test
    @DisplayName("获取最近的审计日志 - 返回空列表")
    void testGetRecentLogs_EmptyList() throws Exception {
        // Given
        when(auditLogService.getRecentLogs(anyInt())).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/audit/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(20000));

        verify(auditLogService, times(1)).getRecentLogs(20);
    }

    @Test
    @DisplayName("获取最近的审计日志 - 不同限制数量")
    void testGetRecentLogs_DifferentLimits() throws Exception {
        // Given
        int[] limits = {10, 20, 50, 100};
        
        for (int limit : limits) {
            List<AuditLog> logs = Arrays.asList(testAuditLog);
            when(auditLogService.getRecentLogs(limit)).thenReturn(logs);

            // When & Then
            mockMvc.perform(get("/api/audit/recent")
                    .param("limit", String.valueOf(limit)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(20000));

            verify(auditLogService, times(1)).getRecentLogs(limit);
        }
    }
}
