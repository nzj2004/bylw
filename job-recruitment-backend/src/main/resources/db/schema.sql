-- 创建数据库
CREATE DATABASE IF NOT EXISTS job_recruitment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE job_recruitment;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    role TINYINT NOT NULL DEFAULT 4 COMMENT '角色：1-管理员 2-运营 3-企业 4-求职者',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    avatar VARCHAR(200) COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 企业信息表
CREATE TABLE IF NOT EXISTS company_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    company_name VARCHAR(100) NOT NULL COMMENT '企业名称',
    industry VARCHAR(50) COMMENT '所属行业',
    scale VARCHAR(50) COMMENT '企业规模',
    address VARCHAR(200) COMMENT '企业地址',
    description TEXT COMMENT '企业简介',
    logo_url VARCHAR(200) COMMENT 'Logo地址',
    website VARCHAR(100) COMMENT '企业官网',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系人电话',
    contact_email VARCHAR(100) COMMENT '联系人邮箱',
    status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已拒绝',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    pending_company_name VARCHAR(100) COMMENT '待审核企业名称',
    pending_industry VARCHAR(50) COMMENT '待审核所属行业',
    pending_scale VARCHAR(50) COMMENT '待审核企业规模',
    pending_address VARCHAR(200) COMMENT '待审核企业地址',
    pending_description TEXT COMMENT '待审核企业简介',
    pending_logo_url VARCHAR(200) COMMENT '待审核Logo地址',
    pending_website VARCHAR(100) COMMENT '待审核企业官网',
    pending_contact_name VARCHAR(50) COMMENT '待审核联系人',
    pending_contact_phone VARCHAR(20) COMMENT '待审核联系电话',
    pending_contact_email VARCHAR(100) COMMENT '待审核联系邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业信息表';

-- 招聘信息表
CREATE TABLE IF NOT EXISTS job_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    company_id BIGINT NOT NULL COMMENT '企业ID',
    title VARCHAR(100) NOT NULL COMMENT '职位标题',
    category VARCHAR(50) COMMENT '职位类别',
    salary_min INT COMMENT '最低薪资',
    salary_max INT COMMENT '最高薪资',
    salary_month INT DEFAULT 12 COMMENT '薪资月数',
    work_city VARCHAR(50) COMMENT '工作城市',
    work_address VARCHAR(200) COMMENT '工作地址',
    experience VARCHAR(50) COMMENT '经验要求',
    education VARCHAR(50) COMMENT '学历要求',
    job_type TINYINT DEFAULT 1 COMMENT '工作类型：1-全职 2-兼职 3-实习',
    job_desc TEXT COMMENT '职位描述',
    requirements TEXT COMMENT '岗位要求',
    welfare TEXT COMMENT '福利待遇',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已发布 2-已拒绝 3-已下线',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    apply_count INT DEFAULT 0 COMMENT '投递次数',
    publish_time DATETIME COMMENT '发布时间',
    deadline DATE COMMENT '截止日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (company_id) REFERENCES company_info(id),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_work_city (work_city),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘信息表';

-- 简历表
CREATE TABLE IF NOT EXISTS resume (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(50) COMMENT '姓名',
    gender TINYINT COMMENT '性别：0-女 1-男',
    birth_date DATE COMMENT '出生日期',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    education VARCHAR(50) COMMENT '最高学历',
    school VARCHAR(100) COMMENT '毕业院校',
    major VARCHAR(100) COMMENT '专业',
    graduation_year INT COMMENT '毕业年份',
    work_experience TEXT COMMENT '工作经历',
    project_exp TEXT COMMENT '项目经验',
    self_eval TEXT COMMENT '自我评价',
    skills TEXT COMMENT '技能特长',
    expected_city VARCHAR(50) COMMENT '期望城市',
    expected_salary_min INT COMMENT '期望最低薪资',
    expected_salary_max INT COMMENT '期望最高薪资',
    attachment_url VARCHAR(200) COMMENT '简历附件',
    is_default TINYINT DEFAULT 1 COMMENT '是否默认简历：0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历表';

-- 投递记录表
CREATE TABLE IF NOT EXISTS application (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    job_id BIGINT NOT NULL COMMENT '职位ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    user_id BIGINT NOT NULL COMMENT '求职者ID',
    company_id BIGINT NOT NULL COMMENT '企业ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待查看 1-已查看 2-感兴趣 3-不合适',
    remark VARCHAR(500) COMMENT '备注',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (job_id) REFERENCES job_info(id),
    FOREIGN KEY (resume_id) REFERENCES resume(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (company_id) REFERENCES company_info(id),
    INDEX idx_job_id (job_id),
    INDEX idx_user_id (user_id),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_job_resume (job_id, resume_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投递记录表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    role_name VARCHAR(20) COMMENT '用户角色',
    operation_type VARCHAR(20) COMMENT '操作类型：LOGIN/LOGOUT/CREATE/UPDATE/DELETE/QUERY/EXPORT/IMPORT/OTHER',
    operation_module VARCHAR(50) COMMENT '操作模块',
    operation_desc VARCHAR(200) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法：GET/POST/PUT/DELETE',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    status TINYINT DEFAULT 1 COMMENT '操作结果：0-失败 1-成功',
    error_msg TEXT COMMENT '错误信息',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    ip_location VARCHAR(100) COMMENT 'IP归属地',
    user_agent VARCHAR(500) COMMENT '用户代理',
    execution_time BIGINT COMMENT '执行时长（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_module (operation_module),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 收藏岗位表
CREATE TABLE IF NOT EXISTS job_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    job_id BIGINT NOT NULL COMMENT '职位ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (job_id) REFERENCES job_info(id),
    UNIQUE KEY uk_user_job (user_id, job_id),
    INDEX idx_user_id (user_id),
    INDEX idx_job_id (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏岗位表';

-- 初始化管理员账号（密码：admin123）
-- 使用 BCrypt 加密后的密码：admin123
INSERT INTO sys_user (username, password, real_name, email, phone, role, status) VALUES
('admin', '$2a$10$xmkvm/L3VkUvz15CwCQoneOXRXMU0uQcrx78Hkced77csCfYZHU86', '系统管理员', 'admin@job.com', '13800138000', 1, 1),
('operator', '$2a$10$xmkvm/L3VkUvz15CwCQoneOXRXMU0uQcrx78Hkced77csCfYZHU86', '运营人员', 'operator@job.com', '13800138001', 2, 1);
