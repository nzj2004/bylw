-- Interview confirmation/reschedule and in-app notifications

SET @col_count := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'interview_rounds'
      AND COLUMN_NAME = 'confirmation_status'
);
SET @sql := IF(@col_count = 0,
    'ALTER TABLE interview_rounds ADD COLUMN confirmation_status TINYINT DEFAULT 0 COMMENT ''confirmation: 0 pending, 1 confirmed, 2 reschedule requested''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_count := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'interview_rounds'
      AND COLUMN_NAME = 'candidate_response_time'
);
SET @sql := IF(@col_count = 0,
    'ALTER TABLE interview_rounds ADD COLUMN candidate_response_time DATETIME COMMENT ''candidate response time''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_count := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'interview_rounds'
      AND COLUMN_NAME = 'reschedule_time'
);
SET @sql := IF(@col_count = 0,
    'ALTER TABLE interview_rounds ADD COLUMN reschedule_time DATETIME COMMENT ''candidate requested reschedule time''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_count := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'interview_rounds'
      AND COLUMN_NAME = 'reschedule_reason'
);
SET @sql := IF(@col_count = 0,
    'ALTER TABLE interview_rounds ADD COLUMN reschedule_reason VARCHAR(500) COMMENT ''candidate reschedule reason''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS user_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'primary id',
    user_id BIGINT NOT NULL COMMENT 'receiver user id',
    title VARCHAR(120) NOT NULL COMMENT 'notification title',
    content VARCHAR(1000) COMMENT 'notification content',
    notification_type VARCHAR(30) COMMENT 'notification type',
    business_type VARCHAR(30) COMMENT 'related business type',
    business_id BIGINT COMMENT 'related business id',
    read_status TINYINT DEFAULT 0 COMMENT 'read status: 0 unread, 1 read',
    read_time DATETIME COMMENT 'read time',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    deleted TINYINT DEFAULT 0 COMMENT 'deleted flag',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_read (user_id, read_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user notification';
