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

@Aspect
@Component
public class AuditLogAspect {
    private static final Log log = LogFactory.getLog(AuditLogAspect.class);
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
        Object result = null;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = RequestContextHolder.currentRequestAttributes() instanceof ServletRequestAttributes
                ? ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRemoteAddr()
                : "unknown";

        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            /*
             * 创建并保存审计日志记录
             * 记录用户名、操作类型、资源类型、IP地址和操作详情
             */
            // 创建审计日志对象
            AuditLog log = AuditLog.builder()
                    .username(username)
                    .action(auditLogRecord.action())
                    .resource(auditLogRecord.resource())
                    .ipAddress(ip)
                    .details(Arrays.toString(joinPoint.getArgs()))
                    .build();
            // 保存审计日志
            auditLogService.saveLog(log);
        }
    }
}
