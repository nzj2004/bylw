package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_notification")
public class UserNotification extends BaseEntity {

    private Long userId;
    private String title;
    private String content;
    private String notificationType;
    private String businessType;
    private Long businessId;
    private Integer readStatus;
    private LocalDateTime readTime;

    public static final int UNREAD = 0;
    public static final int READ = 1;
}
