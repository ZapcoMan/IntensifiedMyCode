package com.example.service.impl;


import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import com.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void sendNotification(Notification notification) {
        notificationMapper.insert(notification);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.findByUser(userId);
    }

    @Override
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationMapper.deleteLogical(id);
    }
}
