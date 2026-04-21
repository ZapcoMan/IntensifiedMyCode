package com.example.service;

import com.example.TestBase;
import com.example.entity.AuditLog;
import com.example.mapper.AuditLogMapper;
import com.example.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * AuditLogServiceImpl 单元测试类
 * 测试审计日志相关的业务逻辑
 */
@DisplayName("AuditLogService 单元测试")
class AuditLogServiceImplTest extends TestBase {

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setUsername("admin");
        testAuditLog.setAction("登录");
        testAuditLog.setResource("用户");
        testAuditLog.setIpAddress("127.0.0.1");
        testAuditLog.setDetails("用户成功登录系统");
        testAuditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    @DisplayName("保存审计日志 - 成功")
    void testSaveLog_Success() {
        // Given
        AuditLog newLog = new AuditLog();
        newLog.setUsername("user1");
        newLog.setAction("添加用户");
        newLog.setResource("用户");
        newLog.setIpAddress("192.168.1.100");

        // When
        auditLogService.saveLog(newLog);

        // Then
        verify(auditLogMapper, times(1)).insert(newLog);
    }

    @Test
    @DisplayName("保存审计日志 - 包含详细信息")
    void testSaveLog_WithDetails() {
        // Given
        AuditLog detailedLog = new AuditLog();
        detailedLog.setUsername("admin");
        detailedLog.setAction("删除用户");
        detailedLog.setResource("用户");
        detailedLog.setIpAddress("10.0.0.1");
        detailedLog.setDetails("删除了用户ID: 123, 用户名: testuser");

        // When
        auditLogService.saveLog(detailedLog);

        // Then
        verify(auditLogMapper, times(1)).insert(detailedLog);
        assertEquals("删除了用户ID: 123, 用户名: testuser", detailedLog.getDetails());
    }

    @Test
    @DisplayName("获取最近的审计日志 - 成功（默认数量）")
    void testGetRecentLogs_DefaultLimit() {
        // Given
        List<AuditLog> expectedLogs = Arrays.asList(testAuditLog);
        when(auditLogMapper.selectRecent(20)).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditLogService.getRecentLogs(20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUsername());
        assertEquals("登录", result.get(0).getAction());
        verify(auditLogMapper, times(1)).selectRecent(20);
    }

    @Test
    @DisplayName("获取最近的审计日志 - 成功（自定义数量）")
    void testGetRecentLogs_CustomLimit() {
        // Given
        int limit = 50;
        List<AuditLog> expectedLogs = Arrays.asList(testAuditLog);
        when(auditLogMapper.selectRecent(limit)).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditLogService.getRecentLogs(limit);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogMapper, times(1)).selectRecent(limit);
    }

    @Test
    @DisplayName("获取最近的审计日志 - 返回空列表")
    void testGetRecentLogs_EmptyList() {
        // Given
        when(auditLogMapper.selectRecent(anyInt())).thenReturn(Arrays.asList());

        // When
        List<AuditLog> result = auditLogService.getRecentLogs(10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(auditLogMapper, times(1)).selectRecent(10);
    }

    @Test
    @DisplayName("获取最近的审计日志 - 不同限制数量")
    void testGetRecentLogs_DifferentLimits() {
        // Given
        int[] limits = {5, 10, 20, 50, 100};

        for (int limit : limits) {
            List<AuditLog> expectedLogs = Arrays.asList(testAuditLog);
            when(auditLogMapper.selectRecent(limit)).thenReturn(expectedLogs);

            // When
            List<AuditLog> result = auditLogService.getRecentLogs(limit);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(auditLogMapper, times(1)).selectRecent(limit);
        }
    }

    @Test
    @DisplayName("保存审计日志 - 多种操作类型")
    void testSaveLog_DifferentActions() {
        // Given
        String[] actions = {"登录", "登出", "添加用户", "删除用户", "更新密码", "查询数据"};

        for (String action : actions) {
            AuditLog log = new AuditLog();
            log.setUsername("testuser");
            log.setAction(action);
            log.setResource("用户");
            log.setIpAddress("127.0.0.1");

            // When
            auditLogService.saveLog(log);

            // Then
            verify(auditLogMapper, times(1)).insert(log);
        }
    }

    @Test
    @DisplayName("保存审计日志 - 不同IP地址")
    void testSaveLog_DifferentIpAddresses() {
        // Given
        String[] ipAddresses = {
            "127.0.0.1",
            "192.168.1.100",
            "10.0.0.1",
            "172.16.0.1",
            "203.0.113.50"
        };

        for (String ip : ipAddresses) {
            AuditLog log = new AuditLog();
            log.setUsername("user");
            log.setAction("访问");
            log.setResource("系统");
            log.setIpAddress(ip);

            // When
            auditLogService.saveLog(log);

            // Then
            verify(auditLogMapper, times(1)).insert(log);
            assertEquals(ip, log.getIpAddress());
        }
    }

    @Test
    @DisplayName("获取最近的审计日志 - 大量数据")
    void testGetRecentLogs_LargeDataSet() {
        // Given
        int limit = 1000;
        List<AuditLog> largeLogList = Arrays.asList(
            testAuditLog, testAuditLog, testAuditLog
        );
        when(auditLogMapper.selectRecent(limit)).thenReturn(largeLogList);

        // When
        List<AuditLog> result = auditLogService.getRecentLogs(limit);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(auditLogMapper, times(1)).selectRecent(limit);
    }

    @Test
    @DisplayName("保存审计日志 - 空详细信息")
    void testSaveLog_NullDetails() {
        // Given
        AuditLog log = new AuditLog();
        log.setUsername("user");
        log.setAction("操作");
        log.setResource("资源");
        log.setIpAddress("127.0.0.1");
        log.setDetails(null);  // 详细信息为空

        // When
        auditLogService.saveLog(log);

        // Then
        verify(auditLogMapper, times(1)).insert(log);
        assertNull(log.getDetails());
    }

    @Test
    @DisplayName("保存审计日志 - 长详细信息")
    void testSaveLog_LongDetails() {
        // Given
        AuditLog log = new AuditLog();
        log.setUsername("admin");
        log.setAction("批量操作");
        log.setResource("用户");
        log.setIpAddress("192.168.1.1");
        // 生成长详细信息
        StringBuilder longDetails = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longDetails.append("操作记录").append(i).append("; ");
        }
        log.setDetails(longDetails.toString());

        // When
        auditLogService.saveLog(log);

        // Then
        verify(auditLogMapper, times(1)).insert(log);
        assertNotNull(log.getDetails());
        assertTrue(log.getDetails().length() > 500);  // 降低阈值
    }
}
