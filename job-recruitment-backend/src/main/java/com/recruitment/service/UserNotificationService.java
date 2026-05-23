package com.recruitment.service;

import com.recruitment.entity.UserNotification;
import com.recruitment.mapper.UserNotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationService {

    @Autowired
    private UserNotificationMapper notificationMapper;

    public void notify(Long userId, String title, String content, String notificationType, String businessType, Long businessId) {
        if (userId == null || title == null || title.isBlank()) {
            return;
        }
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType(notificationType);
        notification.setBusinessType(businessType);
        notification.setBusinessId(businessId);
        notification.setReadStatus(UserNotification.UNREAD);
        notificationMapper.insert(notification);
    }
}
