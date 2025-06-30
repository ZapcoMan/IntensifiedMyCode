package com.example.service;



import com.example.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotification(Notification notification);
    List<Notification> getUserNotifications(Long userId);
    void markAsRead(Long id);
    void deleteNotification(Long id);
}

