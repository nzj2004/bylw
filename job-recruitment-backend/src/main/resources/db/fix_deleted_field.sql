-- 修复 deleted 字段为 NULL 的记录
UPDATE sys_user SET deleted = 0 WHERE deleted IS NULL;
UPDATE sys_company SET deleted = 0 WHERE deleted IS NULL;
UPDATE sys_job SET deleted = 0 WHERE deleted IS NULL;
UPDATE sys_job_application SET deleted = 0 WHERE deleted IS NULL;
UPDATE sys_resume SET deleted = 0 WHERE deleted IS NULL;
