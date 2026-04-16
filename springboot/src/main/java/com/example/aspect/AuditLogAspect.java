package com.example.aspect;

import com.example.annotation.AuditLogRecord;
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
import java.util.stream.Collectors;

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
            
            // 记录成功的审计日志（脱敏处理）
            long duration = System.currentTimeMillis() - startTime;
            String safeDetails = sanitizeArgs(joinPoint.getArgs());
            AuditLog successLog = AuditLog.builder()
                    .username(username)
                    .action(auditLogRecord.action())
                    .resource(auditLogRecord.resource())
                    .ipAddress(ip)
                    .details(safeDetails)
                    .build();
            auditLogService.saveLog(successLog);
            
            log.info(String.format("审计日志 - 用户: %s, 操作: %s, 资源: %s, IP: %s, 耗时: %dms, 状态: 成功",
                    username, auditLogRecord.action(), auditLogRecord.resource(), ip, duration));
            
            return result;
        } catch (Throwable e) {
            // 记录失败的审计日志（脱敏处理）
            long duration = System.currentTimeMillis() - startTime;
            String safeDetails = sanitizeArgs(joinPoint.getArgs());
            AuditLog errorLog = AuditLog.builder()
                    .username(username)
                    .action(auditLogRecord.action())
                    .resource(auditLogRecord.resource())
                    .ipAddress(ip)
                    .details(safeDetails + " | 异常: " + e.getMessage())
                    .build();
            auditLogService.saveLog(errorLog);
            
            log.error(String.format("审计日志 - 用户: %s, 操作: %s, 资源: %s, IP: %s, 耗时: %dms, 状态: 失败, 异常: %s",
                    username, auditLogRecord.action(), auditLogRecord.resource(), ip, duration, e.getMessage()), e);
            
            // 继续抛出异常，让上层处理
            throw e;
        }
    }

    /**
     * 脱敏处理方法参数，防止密码等敏感信息泄露
     * @param args 方法参数数组
     * @return 脱敏后的参数字符串
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        // 将参数数组转换为字符串，并对每个参数进行脱敏处理
        String sanitized = Arrays.stream(args)
                .map(this::sanitizeArg)
                .collect(Collectors.joining(", "));
        
        return "[" + sanitized + "]";
    }

    /**
     * 脱敏单个参数
     * @param arg 参数对象
     * @return 脱敏后的字符串
     */
    private String sanitizeArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        
        String argStr = arg.toString();
        
        // 检测并脱敏密码字段（不区分大小写）
        // 匹配模式: password=xxx, pwd=xxx, newPassword=xxx 等
        if (argStr.toLowerCase().contains("password") || argStr.toLowerCase().contains("pwd")) {
            // 使用正则表达式替换密码值
            argStr = argStr.replaceAll("(?i)(password|pwd|newPassword|oldPassword)\\s*=\\s*[^,}\\]]+", "$1=***");
        }
        
        return argStr;
    }

}
