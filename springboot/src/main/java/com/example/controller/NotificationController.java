package com.example.controller;


import com.example.annotation.AuditLogRecord;
import com.example.common.R;
import com.example.common.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.Notification;
import com.example.enums.RoleEnum;
import com.example.service.NotificationService;
import com.example.utils.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {
    private static final Log log = LogFactory.getLog(NotificationController.class);

    @Resource
    private NotificationService notificationService;

    @Resource
    private TokenUtils tokenUtils;

    @Operation(summary = "发送通知（仅管理员）")
    @AuditLogRecord(action = "发送通知", resource = "通知")
    @PostMapping("/send")
    public R sendNotification(@RequestBody Notification notification) {
        // 1. 获取当前用户
        Account currentUser = tokenUtils.getCurrentUser();
        if (currentUser == null) {
            return R.error(ResultCodeEnum.TOKEN_INVALID, "未登录或Token已失效");
        }

        // 2. 检查是否为管理员角色
        if (!RoleEnum.isAdminRole(currentUser.getRole())) {
            log.warn("非管理员尝试发送通知: userId=" + currentUser.getId() + ", role=" + currentUser.getRole());
            return R.error(Integer.valueOf("403"), "权限不足，只有管理员可以发送通知");
        }

        // 3. 发送通知
        log.info("管理员发送通知: adminId=" + currentUser.getId() + ", targetUserId=" + notification.getUserId());
        notificationService.sendNotification(notification);
        return R.success("sent");
    }

    @Operation(summary = "获取用户通知")
    @AuditLogRecord(action = "获取用户通知", resource = "通知")
    @GetMapping("/user/{userId}")
    public R getUserNotifications(@PathVariable Long userId) {
        log.info("获取用户通知");
        return R.success(notificationService.getUserNotifications(userId));
    }

    @Operation(summary = "标记通知为已读")
    @AuditLogRecord(action = "标记通知为已读", resource = "通知")
    @PostMapping("/{id}/read")
    public R markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return R.success("marked as read");
    }

    @Operation(summary = "删除通知")
    @AuditLogRecord(action = "删除通知", resource = "通知")
    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return R.success("deleted");
    }
}
