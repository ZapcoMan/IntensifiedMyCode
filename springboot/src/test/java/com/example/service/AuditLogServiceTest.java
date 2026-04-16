package com.example.service;

import com.example.entity.AuditLog;
import com.example.mapper.AuditLogMapper;
import com.example.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuditLogServiceImpl 单元测试
 * 最简单的 Service，无 Redis 依赖，优先写
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditLogServiceTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = AuditLog.builder()
                .username("admin1")
                .action("用户登录")
                .resource("/api/admin/login")
                .ipAddress("127.0.0.1")
                .details("{\"username\":\"admin1\"}")
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    @Test
    void saveLog_正常保存() {
        // given
        doNothing().when(auditLogMapper).insert(any(AuditLog.class));

        // when
        auditLogService.saveLog(sampleLog);

        // then
        verify(auditLogMapper, times(1)).insert(sampleLog);
    }

    @Test
    void saveLog_保存空对象也不抛异常() {
        // given
        doNothing().when(auditLogMapper).insert(any(AuditLog.class));

        // when
        auditLogService.saveLog(AuditLog.builder().build());

        // then: 不抛异常即可
        verify(auditLogMapper, times(1)).insert(any(AuditLog.class));
    }

    @Test
    void getRecentLogs_正常返回10条() {
        // given
        List<AuditLog> logs = Arrays.asList(sampleLog, sampleLog, sampleLog);
        when(auditLogMapper.selectRecent(10)).thenReturn(logs);

        // when
        List<AuditLog> result = auditLogService.getRecentLogs(10);

        // then
        assertEquals(3, result.size());
        verify(auditLogMapper).selectRecent(10);
    }

    @Test
    void getRecentLogs_limit为0返回空列表() {
        // given
        when(auditLogMapper.selectRecent(0)).thenReturn(Collections.emptyList());

        // when
        List<AuditLog> result = auditLogService.getRecentLogs(0);

        // then
        assertTrue(result.isEmpty());
        verify(auditLogMapper).selectRecent(0);
    }

    @Test
    void getRecentLogs_limit很大返回所有记录() {
        // given
        List<AuditLog> logs = Arrays.asList(sampleLog, sampleLog);
        when(auditLogMapper.selectRecent(1000)).thenReturn(logs);

        // when
        List<AuditLog> result = auditLogService.getRecentLogs(1000);

        // then
        assertEquals(2, result.size());
        verify(auditLogMapper).selectRecent(1000);
    }

    @Test
    void getRecentLogs_数据库无记录返回空列表() {
        // given
        when(auditLogMapper.selectRecent(10)).thenReturn(Collections.emptyList());

        // when
        List<AuditLog> result = auditLogService.getRecentLogs(10);

        // then
        assertTrue(result.isEmpty());
    }
}
