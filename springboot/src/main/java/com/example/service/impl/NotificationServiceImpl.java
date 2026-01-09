package com.example.service.impl;

import java.util.List;

import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import com.example.service.NotificationService;
import com.example.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    /**
     * 注入通知数据访问对象
     */
    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 发送通知
     *
     * @param notification 待发送的通知对象
     */
    @Override
    public void sendNotification(Notification notification) {
        notificationMapper.insert(notification);
        
        // 发送通知后，清除相关用户的缓存
        String cacheKey = "notification:user:" + notification.getUserId();
        redisUtils.remove(cacheKey);
    }

    /**
     * 获取用户的通知列表
     *
     * @param userId 用户ID
     * @return 用户的通知列表
     */
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        // 先从Redis获取缓存的通知数据
        String cacheKey = "notification:user:" + userId;
        List<Notification> cachedNotifications = redisUtils.get(cacheKey);
        
        if (cachedNotifications != null && !cachedNotifications.isEmpty()) {
            return cachedNotifications;
        }
        
        // 如果Redis中没有，则从数据库查询
        List<Notification> notifications = notificationMapper.findByUser(userId);
        
        // 将结果缓存到Redis，设置有效期为30分钟
        redisUtils.set(cacheKey, notifications, 30, java.util.concurrent.TimeUnit.MINUTES);
        
        return notifications;
    }

    /**
     * 将通知标记为已读
     *
     * @param id 通知的ID
     */
    @Override
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
        
        // 标记为已读后，清除相关用户的缓存
        // 需要查询通知的userId来清除对应缓存
        Notification notification = notificationMapper.findById(id);
        if (notification != null) {
            String cacheKey = "notification:user:" + notification.getUserId();
            redisUtils.remove(cacheKey);
        }
    }

    /**
     * 删除通知
     *
     * @param id 通知的ID
     */
    @Override
    public void deleteNotification(Long id) {
        notificationMapper.deleteLogical(id);
        
        // 删除通知后，清除相关用户的缓存
        // 需要查询通知的userId来清除对应缓存
        Notification notification = notificationMapper.findById(id);
        if (notification != null) {
            String cacheKey = "notification:user:" + notification.getUserId();
            redisUtils.remove(cacheKey);
        }
    }
}