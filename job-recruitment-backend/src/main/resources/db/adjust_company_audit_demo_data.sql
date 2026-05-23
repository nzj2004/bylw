USE job_recruitment;
SET NAMES utf8mb4;

-- Keep a few companies in pending/rejected states so the operator audit flow can be tested.
UPDATE company_info c
JOIN sys_user u ON u.id = c.user_id
SET c.status = 0,
    c.reject_reason = '',
    c.pending_company_name = NULL,
    c.pending_industry = NULL,
    c.pending_scale = NULL,
    c.pending_address = NULL,
    c.pending_description = NULL,
    c.pending_logo_url = NULL,
    c.pending_website = NULL,
    c.pending_contact_name = NULL,
    c.pending_contact_phone = NULL,
    c.pending_contact_email = NULL
WHERE u.username IN ('corp0040', 'corp0041', 'corp0042', 'corp0043');

UPDATE company_info c
JOIN sys_user u ON u.id = c.user_id
SET c.status = 2,
    c.reject_reason = '企业资料与招聘资质不完整，请补充营业信息和联系人后重新提交审核。',
    c.pending_company_name = NULL,
    c.pending_industry = NULL,
    c.pending_scale = NULL,
    c.pending_address = NULL,
    c.pending_description = NULL,
    c.pending_logo_url = NULL,
    c.pending_website = NULL,
    c.pending_contact_name = NULL,
    c.pending_contact_phone = NULL,
    c.pending_contact_email = NULL
WHERE u.username IN ('corp0044', 'corp0045', 'corp0046', 'corp0047');

UPDATE job_info j
JOIN company_info c ON c.id = j.company_id
JOIN sys_user u ON u.id = c.user_id
SET j.status = 3
WHERE u.username IN (
    'corp0040', 'corp0041', 'corp0042', 'corp0043',
    'corp0044', 'corp0045', 'corp0046', 'corp0047'
);
