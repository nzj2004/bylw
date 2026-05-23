-- Recruitment process extension for full hiring flow (Navicat friendly)
-- Compatible with MySQL versions that do NOT support ADD COLUMN IF NOT EXISTS

-- 1) Add application viewed fields if they do not exist
SET @col_count := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'application'
      AND COLUMN_NAME = 'viewed_at'
);
SET @sql := IF(@col_count = 0,
    'ALTER TABLE application ADD COLUMN viewed_at DATETIME COMMENT ''查看时间''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_count := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'application'
      AND COLUMN_NAME = 'viewed_by'
);
SET @sql := IF(@col_count = 0,
    'ALTER TABLE application ADD COLUMN viewed_by BIGINT COMMENT ''查看人ID''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Interview rounds table
CREATE TABLE IF NOT EXISTS interview_rounds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'primary id',
    application_id BIGINT NOT NULL COMMENT 'application id',
    round_no INT NOT NULL COMMENT 'interview round number',
    interview_type VARCHAR(20) DEFAULT 'online' COMMENT 'interview type',
    interview_time DATETIME COMMENT 'interview time',
    location VARCHAR(200) COMMENT 'interview location',
    meeting_link VARCHAR(255) COMMENT 'online meeting link',
    contact_person VARCHAR(50) COMMENT 'contact person',
    contact_phone VARCHAR(20) COMMENT 'contact phone',
    result TINYINT DEFAULT 0 COMMENT 'result: 0 pending, 1 pass, 2 fail, 3 cancel',
    notes TEXT COMMENT 'interview notes',
    confirmation_status TINYINT DEFAULT 0 COMMENT 'confirmation: 0 pending, 1 confirmed, 2 reschedule requested',
    candidate_response_time DATETIME COMMENT 'candidate response time',
    reschedule_time DATETIME COMMENT 'candidate requested reschedule time',
    reschedule_reason VARCHAR(500) COMMENT 'candidate reschedule reason',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    deleted TINYINT DEFAULT 0 COMMENT 'deleted flag',
    FOREIGN KEY (application_id) REFERENCES application(id),
    UNIQUE KEY uk_application_round (application_id, round_no),
    INDEX idx_application_id (application_id),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='interview rounds';

-- 3) Offer/employment table
CREATE TABLE IF NOT EXISTS offers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'primary id',
    application_id BIGINT NOT NULL COMMENT 'application id',
    status TINYINT DEFAULT 0 COMMENT 'status: 0 pending, 1 accepted, 2 rejected, 3 expired',
    offer_title VARCHAR(120) COMMENT 'offer title',
    salary_min INT COMMENT 'salary min',
    salary_max INT COMMENT 'salary max',
    benefits TEXT COMMENT 'benefits',
    work_location VARCHAR(120) COMMENT 'work location',
    offer_content TEXT COMMENT 'offer content',
    entry_date DATE COMMENT 'entry date',
    expire_time DATETIME COMMENT 'offer expire time',
    response_time DATETIME COMMENT 'candidate response time',
    response_comment TEXT COMMENT 'candidate response comment',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    deleted TINYINT DEFAULT 0 COMMENT 'deleted flag',
    FOREIGN KEY (application_id) REFERENCES application(id),
    UNIQUE KEY uk_application_id (application_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='offer and onboarding';

-- 4) Application status history table
CREATE TABLE IF NOT EXISTS application_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'primary id',
    application_id BIGINT NOT NULL COMMENT 'application id',
    old_status TINYINT COMMENT 'old application status',
    new_status TINYINT NOT NULL COMMENT 'new application status',
    changed_by BIGINT COMMENT 'operator user id',
    changed_role VARCHAR(30) COMMENT 'operator role name',
    action VARCHAR(50) COMMENT 'business action',
    remark VARCHAR(500) COMMENT 'change remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    deleted TINYINT DEFAULT 0 COMMENT 'deleted flag',
    FOREIGN KEY (application_id) REFERENCES application(id),
    INDEX idx_application_id (application_id),
    INDEX idx_new_status (new_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='application status history';

-- 5) User notification table
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

INSERT INTO application_status_history
    (application_id, old_status, new_status, changed_by, changed_role, action, remark, create_time, update_time, deleted)
SELECT
    a.id,
    NULL,
    a.status,
    NULL,
    'SYSTEM',
    'HISTORY_BACKFILL',
    '历史投递状态初始化',
    COALESCE(a.handle_time, a.apply_time, a.create_time, CURRENT_TIMESTAMP),
    CURRENT_TIMESTAMP,
    0
FROM application a
WHERE a.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM application_status_history h
      WHERE h.application_id = a.id
        AND h.deleted = 0
  );
