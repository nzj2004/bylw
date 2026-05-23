USE job_recruitment;
SET NAMES utf8mb4;

ALTER TABLE company_info
    ADD COLUMN pending_company_name VARCHAR(100) NULL COMMENT 'pending company name' AFTER reject_reason,
    ADD COLUMN pending_industry VARCHAR(50) NULL COMMENT 'pending industry' AFTER pending_company_name,
    ADD COLUMN pending_scale VARCHAR(50) NULL COMMENT 'pending scale' AFTER pending_industry,
    ADD COLUMN pending_address VARCHAR(200) NULL COMMENT 'pending address' AFTER pending_scale,
    ADD COLUMN pending_description TEXT NULL COMMENT 'pending description' AFTER pending_address,
    ADD COLUMN pending_logo_url VARCHAR(200) NULL COMMENT 'pending logo url' AFTER pending_description,
    ADD COLUMN pending_website VARCHAR(100) NULL COMMENT 'pending website' AFTER pending_logo_url,
    ADD COLUMN pending_contact_name VARCHAR(50) NULL COMMENT 'pending contact name' AFTER pending_website,
    ADD COLUMN pending_contact_phone VARCHAR(20) NULL COMMENT 'pending contact phone' AFTER pending_contact_name,
    ADD COLUMN pending_contact_email VARCHAR(100) NULL COMMENT 'pending contact email' AFTER pending_contact_phone;
