-- Clean deterministic demo data for the recruitment system.
-- WARNING: this seed script resets demo business data.
-- For fixing the current database without deleting existing rows, use repair_seed_chinese_data.sql.
-- All seeded accounts use password: admin123.

USE job_recruitment;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @pwd := '$2a$10$xmkvm/L3VkUvz15CwCQoneOXRXMU0uQcrx78Hkced77csCfYZHU86';

-- Keep the script runnable on both old and new schemas.
SET @col_count := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'application' AND COLUMN_NAME = 'viewed_at');
SET @sql := IF(@col_count = 0, 'ALTER TABLE application ADD COLUMN viewed_at DATETIME NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_count := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'application' AND COLUMN_NAME = 'viewed_by');
SET @sql := IF(@col_count = 0, 'ALTER TABLE application ADD COLUMN viewed_by BIGINT NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_count := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resume' AND COLUMN_NAME = 'avatar_url');
SET @sql := IF(@col_count = 0, 'ALTER TABLE resume ADD COLUMN avatar_url VARCHAR(255) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_count := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resume' AND COLUMN_NAME = 'pdf_url');
SET @sql := IF(@col_count = 0, 'ALTER TABLE resume ADD COLUMN pdf_url VARCHAR(255) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS interview_rounds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    round_no INT NOT NULL,
    interview_type VARCHAR(20) DEFAULT 'online',
    interview_time DATETIME,
    location VARCHAR(200),
    meeting_link VARCHAR(255),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    result TINYINT DEFAULT 0,
    notes TEXT,
    confirmation_status TINYINT DEFAULT 0,
    candidate_response_time DATETIME,
    reschedule_time DATETIME,
    reschedule_reason VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_application_round (application_id, round_no),
    INDEX idx_application_id (application_id),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @col_count := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interview_rounds' AND COLUMN_NAME = 'confirmation_status');
SET @sql := IF(@col_count = 0, 'ALTER TABLE interview_rounds ADD COLUMN confirmation_status TINYINT DEFAULT 0', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_count := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interview_rounds' AND COLUMN_NAME = 'candidate_response_time');
SET @sql := IF(@col_count = 0, 'ALTER TABLE interview_rounds ADD COLUMN candidate_response_time DATETIME NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_count := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interview_rounds' AND COLUMN_NAME = 'reschedule_time');
SET @sql := IF(@col_count = 0, 'ALTER TABLE interview_rounds ADD COLUMN reschedule_time DATETIME NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_count := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interview_rounds' AND COLUMN_NAME = 'reschedule_reason');
SET @sql := IF(@col_count = 0, 'ALTER TABLE interview_rounds ADD COLUMN reschedule_reason VARCHAR(500) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS offers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0,
    offer_title VARCHAR(120),
    salary_min INT,
    salary_max INT,
    benefits TEXT,
    work_location VARCHAR(120),
    offer_content TEXT,
    entry_date DATE,
    expire_time DATETIME,
    response_time DATETIME,
    response_comment TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_application_id (application_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS application_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    old_status TINYINT,
    new_status TINYINT NOT NULL,
    changed_by BIGINT,
    changed_role VARCHAR(30),
    action VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_application_id (application_id),
    INDEX idx_new_status (new_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(1000),
    notification_type VARCHAR(30),
    business_type VARCHAR(30),
    business_id BIGINT,
    read_status TINYINT DEFAULT 0,
    read_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_read (user_id, read_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELETE FROM application_status_history;
DELETE FROM user_notification;
DELETE FROM offers;
DELETE FROM interview_rounds;
DELETE FROM application;
DELETE FROM job_favorite;
DELETE FROM resume;
DELETE FROM job_info;
DELETE FROM company_info;
DELETE FROM sys_user WHERE role IN (3, 4);

INSERT INTO sys_user (username, password, real_name, email, phone, role, status, deleted)
VALUES
('admin', @pwd, 'System Admin', 'admin@job.com', '13800138000', 1, 1, 0),
('operator', @pwd, 'Platform Operator', 'operator@job.com', '13800138001', 2, 1, 0)
ON DUPLICATE KEY UPDATE password = VALUES(password), real_name = VALUES(real_name), email = VALUES(email), phone = VALUES(phone), role = VALUES(role), status = VALUES(status), deleted = 0;

INSERT INTO sys_user (username, password, real_name, email, phone, role, status, deleted)
VALUES
('alibaba', @pwd, 'Alibaba HR', 'hr@alibaba.com', '13800138002', 3, 1, 0),
('tencent', @pwd, 'Tencent HR', 'hr@tencent.com', '13800138003', 3, 1, 0),
('baidu', @pwd, 'Baidu HR', 'hr@baidu.com', '13800138004', 3, 1, 0),
('zhangsan', @pwd, 'Zhang San', 'zhangsan@qq.com', '13800138005', 4, 1, 0),
('lisi', @pwd, 'Li Si', 'lisi@qq.com', '13800138006', 4, 1, 0),
('wangwu', @pwd, 'Wang Wu', 'wangwu@qq.com', '13800138007', 4, 1, 0),
('zhouhao', @pwd, 'Zhou Hao', 'zhouhao@qq.com', '13800138008', 4, 1, 0);

SELECT id INTO @alibaba_user_id FROM sys_user WHERE username = 'alibaba';
SELECT id INTO @tencent_user_id FROM sys_user WHERE username = 'tencent';
SELECT id INTO @baidu_user_id FROM sys_user WHERE username = 'baidu';
SELECT id INTO @zhangsan_user_id FROM sys_user WHERE username = 'zhangsan';
SELECT id INTO @lisi_user_id FROM sys_user WHERE username = 'lisi';
SELECT id INTO @wangwu_user_id FROM sys_user WHERE username = 'wangwu';
SELECT id INTO @zhouhao_user_id FROM sys_user WHERE username = 'zhouhao';

INSERT INTO company_info (user_id, company_name, industry, scale, address, description, logo_url, website, contact_name, contact_phone, contact_email, status, deleted)
VALUES
(@alibaba_user_id, 'Alibaba Group', 'Internet / Cloud', '10000+', 'Hangzhou', 'E-commerce and cloud computing company.', 'https://logo.alibaba.com', 'https://www.alibaba.com', 'Alibaba HR', '13800138002', 'hr@alibaba.com', 1, 0),
(@tencent_user_id, 'Tencent Technology', 'Internet / Games', '10000+', 'Shenzhen', 'Internet services and gaming company.', 'https://logo.tencent.com', 'https://www.tencent.com', 'Tencent HR', '13800138003', 'hr@tencent.com', 1, 0),
(@baidu_user_id, 'Baidu Online', 'AI / Search', '10000+', 'Beijing', 'Search and artificial intelligence company.', 'https://logo.baidu.com', 'https://www.baidu.com', 'Baidu HR', '13800138004', 'hr@baidu.com', 1, 0);

SELECT id INTO @alibaba_company_id FROM company_info WHERE user_id = @alibaba_user_id;
SELECT id INTO @tencent_company_id FROM company_info WHERE user_id = @tencent_user_id;
SELECT id INTO @baidu_company_id FROM company_info WHERE user_id = @baidu_user_id;

INSERT INTO resume (user_id, real_name, gender, birth_date, phone, email, education, school, major, graduation_year, work_experience, project_exp, self_eval, skills, expected_city, expected_salary_min, expected_salary_max, is_default, deleted)
VALUES
(@zhangsan_user_id, 'Zhang San', 1, '1998-05-15', '13800138005', 'zhangsan@qq.com', 'Bachelor', 'Zhejiang University', 'Computer Science', 2020, 'Java backend engineer.', 'E-commerce order system.', 'Reliable and collaborative.', 'Java, Spring Boot, MySQL, Redis', 'Hangzhou', 15000, 25000, 1, 0),
(@lisi_user_id, 'Li Si', 0, '1999-08-20', '13800138006', 'lisi@qq.com', 'Bachelor', 'Shenzhen University', 'Software Engineering', 2021, 'Frontend engineer.', 'Admin system.', 'Focuses on user experience.', 'Vue, React, JavaScript, TypeScript', 'Shenzhen', 12000, 20000, 1, 0),
(@wangwu_user_id, 'Wang Wu', 1, '1997-03-10', '13800138007', 'wangwu@qq.com', 'Master', 'Tsinghua University', 'Artificial Intelligence', 2022, 'Algorithm engineer.', 'Recommendation system.', 'Strong algorithm foundation.', 'Python, PyTorch, TensorFlow', 'Beijing', 25000, 40000, 1, 0),
(@zhouhao_user_id, 'Zhou Hao', 1, '1999-11-02', '13800138008', 'zhouhao@qq.com', 'Bachelor', 'Shanghai University of Finance', 'Marketing', 2022, 'Marketing operation in fintech.', 'Banking growth project.', 'Good communication and data analysis.', 'Marketing research, SQL, Excel, user growth', 'Shenzhen', 12000, 22000, 1, 0);

SELECT id INTO @zhangsan_resume_id FROM resume WHERE user_id = @zhangsan_user_id AND is_default = 1;
SELECT id INTO @lisi_resume_id FROM resume WHERE user_id = @lisi_user_id AND is_default = 1;
SELECT id INTO @wangwu_resume_id FROM resume WHERE user_id = @wangwu_user_id AND is_default = 1;
SELECT id INTO @zhouhao_resume_id FROM resume WHERE user_id = @zhouhao_user_id AND is_default = 1;

INSERT INTO job_info (company_id, title, category, salary_min, salary_max, salary_month, work_city, work_address, experience, education, job_type, job_desc, requirements, welfare, status, view_count, apply_count, publish_time, deadline, deleted)
VALUES
(@alibaba_company_id, 'Java Backend Engineer', 'Software', 15000, 25000, 14, 'Hangzhou', 'Alibaba Xixi Campus', '3-5 years', 'Bachelor', 1, 'Build core commerce services.', 'Java, Spring Boot, MySQL, Redis.', 'Insurance, bonus, paid leave.', 1, 1250, 0, '2026-02-01 10:00:00', '2026-12-31', 0),
(@alibaba_company_id, 'Algorithm Engineer', 'AI', 25000, 40000, 16, 'Beijing', 'Alibaba Beijing Office', '3-5 years', 'Master', 1, 'Search and recommendation algorithm development.', 'Machine learning and deep learning.', 'Insurance, bonus, stock option.', 1, 2100, 0, '2026-02-15 10:30:00', '2026-12-31', 0),
(@tencent_company_id, 'Market Manager (Banking)', 'Banking', 16000, 26000, 14, 'Shenzhen', 'Tencent Building', '3-5 years', 'Bachelor', 1, 'Financial industry customer growth and brand marketing.', 'Banking business and campaign planning.', 'Insurance, bonus, health care.', 1, 620, 0, '2026-05-20 09:00:00', '2026-12-31', 0),
(@baidu_company_id, 'Brand Manager', 'Marketing', 15000, 28000, 14, 'Beijing', 'Baidu Building', '3-5 years', 'Bachelor', 1, 'Brand communication, content planning and campaign execution.', 'Brand strategy and cross-team collaboration.', 'Insurance, bonus, paid leave.', 1, 850, 0, '2026-05-09 13:45:46', '2026-12-31', 0),
(@baidu_company_id, 'Private Banking Consultant', 'Banking', 18000, 32000, 16, 'Beijing', 'Finance Street', '3-5 years', 'Bachelor', 1, 'High-net-worth customer relationship management.', 'Financial product and relationship management.', 'Insurance, performance bonus.', 1, 500, 0, '2026-04-30 13:43:04', '2026-12-31', 0),
(@baidu_company_id, 'Administrative Specialist', 'Admin', 8000, 13000, 13, 'Beijing', 'Baidu Building', '1-3 years', 'Bachelor', 1, 'Office administration and meeting support.', 'Careful, responsible and good communication.', 'Insurance, paid leave, holiday benefits.', 1, 350, 0, '2026-04-30 13:42:58', '2026-12-31', 0),
(@tencent_company_id, 'Blockchain Developer', 'Software', 20000, 35000, 15, 'Shenzhen', 'Tencent Building', '3-5 years', 'Bachelor', 1, 'Blockchain service and smart contract development.', 'Go, Java or Solidity.', 'Insurance, bonus, flexible work.', 1, 760, 0, '2026-04-30 13:42:54', '2026-12-31', 0);

SELECT id INTO @job_java FROM job_info WHERE title = 'Java Backend Engineer' AND company_id = @alibaba_company_id;
SELECT id INTO @job_algorithm FROM job_info WHERE title = 'Algorithm Engineer' AND company_id = @alibaba_company_id;
SELECT id INTO @job_market FROM job_info WHERE title = 'Market Manager (Banking)' AND company_id = @tencent_company_id;
SELECT id INTO @job_brand FROM job_info WHERE title = 'Brand Manager' AND company_id = @baidu_company_id;
SELECT id INTO @job_private_bank FROM job_info WHERE title = 'Private Banking Consultant' AND company_id = @baidu_company_id;
SELECT id INTO @job_admin FROM job_info WHERE title = 'Administrative Specialist' AND company_id = @baidu_company_id;
SELECT id INTO @job_blockchain FROM job_info WHERE title = 'Blockchain Developer' AND company_id = @tencent_company_id;

INSERT INTO application (job_id, resume_id, user_id, company_id, status, remark, apply_time, handle_time, viewed_at, viewed_by, deleted)
VALUES
(@job_market, @zhouhao_resume_id, @zhouhao_user_id, @tencent_company_id, 0, NULL, '2026-05-23 23:14:14', NULL, NULL, NULL, 0),
(@job_brand, @zhouhao_resume_id, @zhouhao_user_id, @baidu_company_id, 6, 'Offer sent, waiting response', '2026-05-09 13:45:46', '2026-05-20 18:00:00', '2026-05-10 09:00:00', @baidu_user_id, 0),
(@job_private_bank, @zhouhao_resume_id, @zhouhao_user_id, @baidu_company_id, 7, 'Candidate accepted offer', '2026-04-30 13:43:04', '2026-05-15 10:00:00', '2026-05-01 09:30:00', @baidu_user_id, 0),
(@job_admin, @zhouhao_resume_id, @zhouhao_user_id, @baidu_company_id, 4, 'First interview arranged', '2026-04-30 13:42:58', '2026-05-03 14:00:00', '2026-05-01 10:00:00', @baidu_user_id, 0),
(@job_blockchain, @zhouhao_resume_id, @zhouhao_user_id, @tencent_company_id, 10, 'Final interview passed, waiting offer', '2026-04-30 13:42:54', '2026-05-08 17:30:00', '2026-05-01 11:00:00', @tencent_user_id, 0),
(@job_algorithm, @zhouhao_resume_id, @zhouhao_user_id, @alibaba_company_id, 11, 'Offer expired', '2026-02-18 00:00:00', '2026-03-01 00:00:00', '2026-02-19 09:00:00', @alibaba_user_id, 0),
(@job_java, @zhangsan_resume_id, @zhangsan_user_id, @alibaba_company_id, 1, 'Resume viewed', '2026-05-01 09:00:00', '2026-05-02 09:00:00', '2026-05-02 09:00:00', @alibaba_user_id, 0),
(@job_java, @lisi_resume_id, @lisi_user_id, @alibaba_company_id, 2, 'Interested candidate', '2026-05-02 10:00:00', '2026-05-03 10:00:00', '2026-05-03 10:00:00', @alibaba_user_id, 0),
(@job_algorithm, @wangwu_resume_id, @wangwu_user_id, @alibaba_company_id, 4, 'Interviewing', '2026-05-03 11:00:00', '2026-05-04 11:00:00', '2026-05-04 11:00:00', @alibaba_user_id, 0);

SELECT id INTO @app_zhou_market FROM application WHERE job_id = @job_market AND resume_id = @zhouhao_resume_id;
SELECT id INTO @app_zhou_brand FROM application WHERE job_id = @job_brand AND resume_id = @zhouhao_resume_id;
SELECT id INTO @app_zhou_private_bank FROM application WHERE job_id = @job_private_bank AND resume_id = @zhouhao_resume_id;
SELECT id INTO @app_zhou_admin FROM application WHERE job_id = @job_admin AND resume_id = @zhouhao_resume_id;
SELECT id INTO @app_zhou_blockchain FROM application WHERE job_id = @job_blockchain AND resume_id = @zhouhao_resume_id;
SELECT id INTO @app_zhou_algorithm FROM application WHERE job_id = @job_algorithm AND resume_id = @zhouhao_resume_id;

INSERT INTO application_status_history (application_id, old_status, new_status, changed_by, changed_role, action, remark, create_time, deleted)
VALUES
(@app_zhou_market, NULL, 0, @zhouhao_user_id, 'USER', 'APPLY_CREATED', 'Candidate submitted application', '2026-05-23 23:14:14', 0),
(@app_zhou_brand, NULL, 0, @zhouhao_user_id, 'USER', 'APPLY_CREATED', 'Candidate submitted application', '2026-05-09 13:45:46', 0),
(@app_zhou_brand, 0, 1, @baidu_user_id, 'COMPANY', 'RESUME_VIEWED', 'Company viewed resume', '2026-05-10 09:00:00', 0),
(@app_zhou_brand, 1, 6, @baidu_user_id, 'COMPANY', 'OFFER_SENT', 'Company sent offer', '2026-05-20 18:00:00', 0),
(@app_zhou_private_bank, NULL, 0, @zhouhao_user_id, 'USER', 'APPLY_CREATED', 'Candidate submitted application', '2026-04-30 13:43:04', 0),
(@app_zhou_private_bank, 0, 6, @baidu_user_id, 'COMPANY', 'OFFER_SENT', 'Company sent offer', '2026-05-10 10:00:00', 0),
(@app_zhou_private_bank, 6, 7, @zhouhao_user_id, 'USER', 'OFFER_ACCEPTED', 'Candidate accepted offer', '2026-05-15 10:00:00', 0),
(@app_zhou_admin, NULL, 0, @zhouhao_user_id, 'USER', 'APPLY_CREATED', 'Candidate submitted application', '2026-04-30 13:42:58', 0),
(@app_zhou_admin, 0, 1, @baidu_user_id, 'COMPANY', 'RESUME_VIEWED', 'Company viewed resume', '2026-05-01 10:00:00', 0),
(@app_zhou_admin, 1, 4, @baidu_user_id, 'COMPANY', 'INTERVIEW_CREATED', 'First interview arranged', '2026-05-03 14:00:00', 0),
(@app_zhou_blockchain, NULL, 0, @zhouhao_user_id, 'USER', 'APPLY_CREATED', 'Candidate submitted application', '2026-04-30 13:42:54', 0),
(@app_zhou_blockchain, 0, 4, @tencent_user_id, 'COMPANY', 'INTERVIEW_CREATED', 'Interview arranged', '2026-05-05 10:00:00', 0),
(@app_zhou_blockchain, 4, 10, @tencent_user_id, 'COMPANY', 'INTERVIEW_PASSED', 'Final interview passed', '2026-05-08 17:30:00', 0),
(@app_zhou_algorithm, NULL, 0, @zhouhao_user_id, 'USER', 'APPLY_CREATED', 'Candidate submitted application', '2026-02-18 00:00:00', 0),
(@app_zhou_algorithm, 0, 6, @alibaba_user_id, 'COMPANY', 'OFFER_SENT', 'Company sent offer', '2026-02-20 09:00:00', 0),
(@app_zhou_algorithm, 6, 11, NULL, 'SYSTEM', 'OFFER_EXPIRED', 'Offer expired automatically', '2026-03-01 00:00:00', 0);

INSERT INTO interview_rounds (application_id, round_no, interview_type, interview_time, location, meeting_link, contact_person, contact_phone, result, notes, confirmation_status, candidate_response_time, reschedule_time, reschedule_reason, create_time, deleted)
VALUES
(@app_zhou_admin, 1, 'online', '2026-06-05 14:00:00', NULL, 'https://meeting.example.com/admin-1', 'Baidu HR', '13800138004', 0, 'Waiting candidate confirmation', 0, NULL, NULL, NULL, '2026-05-03 14:00:00', 0),
(@app_zhou_blockchain, 1, 'online', '2026-06-06 10:00:00', NULL, 'https://meeting.example.com/blockchain-1', 'Tencent HR', '13800138003', 0, 'Candidate requested reschedule', 2, '2026-05-24 10:00:00', '2026-06-07 15:30:00', 'Schedule conflict, request another time', '2026-05-05 10:00:00', 0),
(@app_zhou_private_bank, 1, 'offline', '2026-05-08 09:30:00', 'Baidu Building 8F', NULL, 'Baidu HR', '13800138004', 1, 'Passed', 1, '2026-05-05 12:00:00', NULL, NULL, '2026-05-02 10:00:00', 0);

INSERT INTO offers (application_id, status, offer_title, salary_min, salary_max, benefits, work_location, offer_content, entry_date, expire_time, response_time, response_comment, create_time, deleted)
VALUES
(@app_zhou_brand, 0, 'Brand Manager Offer', 15000, 28000, 'Insurance, bonus, paid leave', 'Beijing', 'Please respond before the deadline.', '2026-06-15', '2026-06-30 18:00:00', NULL, NULL, '2026-05-20 18:00:00', 0),
(@app_zhou_private_bank, 1, 'Private Banking Consultant Offer', 18000, 32000, 'Insurance, bonus, healthcare', 'Beijing', 'Offer accepted.', '2026-06-01', '2026-05-20 18:00:00', '2026-05-15 10:00:00', 'Accepted, thank you.', '2026-05-10 10:00:00', 0),
(@app_zhou_algorithm, 3, 'Algorithm Engineer Offer', 25000, 40000, 'Insurance, bonus, stock option', 'Beijing', 'Offer expired.', '2026-03-20', '2026-03-01 00:00:00', '2026-03-01 00:00:00', 'Expired automatically.', '2026-02-20 09:00:00', 0);

INSERT INTO job_favorite (user_id, job_id, deleted)
VALUES
(@zhouhao_user_id, @job_market, 0),
(@zhouhao_user_id, @job_brand, 0);

INSERT INTO user_notification (user_id, title, content, notification_type, business_type, business_id, read_status, read_time, create_time, deleted)
VALUES
(@zhouhao_user_id, 'New interview scheduled', 'Administrative Specialist has a new interview. Please confirm or request reschedule.', 'INTERVIEW', 'APPLICATION', @app_zhou_admin, 0, NULL, '2026-05-24 09:00:00', 0),
(@zhouhao_user_id, 'Offer waiting response', 'Brand Manager offer is waiting for your response.', 'APPLICATION', 'APPLICATION', @app_zhou_brand, 0, NULL, '2026-05-24 09:05:00', 0),
(@zhouhao_user_id, 'Offer expired', 'Algorithm Engineer offer has expired.', 'APPLICATION', 'APPLICATION', @app_zhou_algorithm, 1, '2026-03-01 00:05:00', '2026-03-01 00:00:00', 0),
(@zhouhao_user_id, 'Interview reschedule submitted', 'Blockchain Developer reschedule request is waiting for company approval.', 'INTERVIEW', 'APPLICATION', @app_zhou_blockchain, 0, NULL, '2026-05-24 10:00:00', 0),
(@baidu_user_id, 'Candidate accepted offer', 'Zhou Hao accepted Private Banking Consultant offer.', 'OFFER', 'APPLICATION', @app_zhou_private_bank, 0, NULL, '2026-05-15 10:00:00', 0),
(@tencent_user_id, 'Candidate requested reschedule', 'Zhou Hao requested to reschedule Blockchain Developer interview.', 'INTERVIEW', 'APPLICATION', @app_zhou_blockchain, 0, NULL, '2026-05-24 10:00:00', 0);

UPDATE job_info j
SET apply_count = (
    SELECT COUNT(1)
    FROM application a
    WHERE a.job_id = j.id AND a.deleted = 0
);

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'sys_user' AS table_name, COUNT(*) AS count FROM sys_user WHERE deleted = 0
UNION ALL SELECT 'company_info', COUNT(*) FROM company_info WHERE deleted = 0
UNION ALL SELECT 'job_info', COUNT(*) FROM job_info WHERE deleted = 0
UNION ALL SELECT 'resume', COUNT(*) FROM resume WHERE deleted = 0
UNION ALL SELECT 'application', COUNT(*) FROM application WHERE deleted = 0
UNION ALL SELECT 'application_status_history', COUNT(*) FROM application_status_history WHERE deleted = 0
UNION ALL SELECT 'interview_rounds', COUNT(*) FROM interview_rounds WHERE deleted = 0
UNION ALL SELECT 'offers', COUNT(*) FROM offers WHERE deleted = 0
UNION ALL SELECT 'job_favorite', COUNT(*) FROM job_favorite WHERE deleted = 0
UNION ALL SELECT 'user_notification', COUNT(*) FROM user_notification WHERE deleted = 0;

SELECT id, username, role, status
FROM sys_user
WHERE username IN ('admin', 'operator', 'alibaba', 'tencent', 'baidu', 'zhangsan', 'lisi', 'wangwu', 'zhouhao')
ORDER BY role, username;
