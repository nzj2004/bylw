-- Application status history for tracking hiring flow changes

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

-- Optional backfill for existing applications that do not have any history yet.
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
