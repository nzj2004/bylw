-- 为简历表添加头像和PDF字段
USE job_recruitment;

ALTER TABLE resume 
ADD COLUMN avatar_url VARCHAR(200) COMMENT '个人照片URL' AFTER attachment_url,
ADD COLUMN pdf_url VARCHAR(200) COMMENT 'PDF简历URL' AFTER avatar_url;

-- 为企业表确认logo_url字段已存在（如果不存在则添加）
-- ALTER TABLE company_info 
-- ADD COLUMN logo_url VARCHAR(200) COMMENT 'Logo地址' AFTER description;
