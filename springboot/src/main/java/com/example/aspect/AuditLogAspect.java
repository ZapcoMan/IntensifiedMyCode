package com.example.aspect;

import com.example.annotation.AuditLogRecord;
import com.example.controller.NotificationController;
import com.example.entity.AuditLog;
import com.example.service.AuditLogService;


import jakarta.annotation.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

// 定义一个切面类，用于处理审计日志
@Aspect
@Component
public class AuditLogAspect {
    // 日志对象，用于记录审计日志方面的信息
    private static final Log log = LogFactory.getLog(AuditLogAspect.class);

    // 注入审计日志服务，用于保存审计日志
    @Resource
    private AuditLogService auditLogService;

    /**
     * 处理审计日志记录的环绕通知方法
     * @param joinPoint 切点信息，包含被拦截的方法执行信息
     * @param auditLogRecord 注解对象，包含审计日志的元数据
     * @return Object 被拦截方法的执行结果
     * @throws Throwable 方法执行过程中可能抛出的异常
     */
    @Around("@annotation(auditLogRecord)")
    public Object handleAudit(ProceedingJoinPoint joinPoint, AuditLogRecord auditLogRecord) throws Throwable {
        // 获取当前登录用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 获取客户端IP地址
        String ip = RequestContextHolder.currentRequestAttributes() instanceof ServletRequestAttributes
                ? ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRemoteAddr()
                : "unknown";

        long startTime = System.currentTimeMillis();
        
        try {
            // 执行被拦截的方法
            Object result = joinPoint.proceed();
            
            // 记录成功的审计日志
            long duration = System.currentTimeMillis() - startTime;
            AuditLog successLog = AuditLog.builder()
                    .username(username)
                    .action(auditLogRecord.action())
                    .resource(auditLogRecord.resource())
                    .ipAddress(ip)
                    .details(Arrays.toString(joinPoint.getArgs()))
                    .build();
            auditLogService.saveLog(successLog);
            
            log.info(String.format("审计日志 - 用户: %s, 操作: %s, 资源: %s, IP: %s, 耗时: %dms, 状态: 成功",
                    username, auditLogRecord.action(), auditLogRecord.resource(), ip, duration));
            
            return result;
        } catch (Throwable e) {
            // 记录失败的审计日志
            long duration = System.currentTimeMillis() - startTime;
            AuditLog errorLog = AuditLog.builder()
                    .username(username)
                    .action(auditLogRecord.action())
                    .resource(auditLogRecord.resource())
                    .ipAddress(ip)
                    .details(Arrays.toString(joinPoint.getArgs()) + " | 异常: " + e.getMessage())
                    .build();
            auditLogService.saveLog(errorLog);
            
            log.error(String.format("审计日志 - 用户: %s, 操作: %s, 资源: %s, IP: %s, 耗时: %dms, 状态: 失败, 异常: %s",
                    username, auditLogRecord.action(), auditLogRecord.resource(), ip, duration, e.getMessage()), e);
            
            // 继续抛出异常，让上层处理
            throw e;
        }
    }

}
