package com.recruitment.dto;

import lombok.Data;

@Data
public class UserNotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String notificationType;
    private String businessType;
    private Long businessId;
    private Integer readStatus;
    private String readTime;
    private String createTime;
}
