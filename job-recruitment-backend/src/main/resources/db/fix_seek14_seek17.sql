-- 为 seek14 / seek17 强制恢复为求职者角色（兼容旧版 MySQL）
-- 先查当前角色
SELECT
    id,
    username,
    `role`,
    status
FROM sys_user
WHERE username IN ('seek14', 'seek17')
ORDER BY id;

-- 强制设置为求职者（4）并置为启用状态
UPDATE sys_user
SET `role` = 4,
    `status` = 1
WHERE username IN ('seek14', 'seek17');

-- 再次核验
SELECT
    id,
    username,
    `role`,
    status,
    CASE
        WHEN `role` = 1 THEN '管理员'
        WHEN `role` = 2 THEN '运营'
        WHEN `role` = 3 THEN '企业'
        WHEN `role` = 4 THEN '求职者'
        ELSE '未知'
    END AS role_name
FROM sys_user
WHERE username IN ('seek14', 'seek17')
ORDER BY id;