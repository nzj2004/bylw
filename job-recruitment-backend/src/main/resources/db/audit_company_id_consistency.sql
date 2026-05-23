-- Company ID consistency audit after unifying company_id to company_info.id
-- Run the SELECT statements first. The UPDATE statements are intentionally commented.

-- 1) job_info rows whose company_id cannot match company_info.id
SELECT
    j.id AS job_id,
    j.title,
    j.company_id,
    c_by_user.id AS suspected_company_info_id,
    c_by_user.user_id AS suspected_company_user_id,
    c_by_user.company_name AS suspected_company_name
FROM job_info j
LEFT JOIN company_info c_by_id ON j.company_id = c_by_id.id
LEFT JOIN company_info c_by_user ON j.company_id = c_by_user.user_id
WHERE j.deleted = 0
  AND c_by_id.id IS NULL;

-- 2) application rows whose company_id cannot match company_info.id
SELECT
    a.id AS application_id,
    a.job_id,
    a.user_id,
    a.company_id,
    c_by_user.id AS suspected_company_info_id,
    c_by_user.user_id AS suspected_company_user_id,
    c_by_user.company_name AS suspected_company_name
FROM application a
LEFT JOIN company_info c_by_id ON a.company_id = c_by_id.id
LEFT JOIN company_info c_by_user ON a.company_id = c_by_user.user_id
WHERE a.deleted = 0
  AND c_by_id.id IS NULL;

-- 3) Optional repair for rows that were written with company_info.user_id.
-- Review the SELECT results before uncommenting.
--
-- UPDATE job_info j
-- JOIN company_info c ON j.company_id = c.user_id
-- LEFT JOIN company_info c_by_id ON j.company_id = c_by_id.id
-- SET j.company_id = c.id,
--     j.update_time = CURRENT_TIMESTAMP
-- WHERE j.deleted = 0
--   AND c_by_id.id IS NULL;
--
-- UPDATE application a
-- JOIN company_info c ON a.company_id = c.user_id
-- LEFT JOIN company_info c_by_id ON a.company_id = c_by_id.id
-- SET a.company_id = c.id,
--     a.update_time = CURRENT_TIMESTAMP
-- WHERE a.deleted = 0
--   AND c_by_id.id IS NULL;
