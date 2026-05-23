-- 校验所有用户的角色值
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
        ELSE '未知角色'
    END AS role_name,
    create_time
FROM sys_user
ORDER BY id;

-- 你需要临时让某个账号作为“求职者测试”时，先执行查一次：
-- SELECT id, username, `role` FROM sys_user WHERE username = '你的用户名';

-- 如该账号角色不是4，可以修复成求职者（4）
-- 注意：请先把 'your_user' 改成真实账号
UPDATE sys_user
SET `role` = 4,
    `status` = 1
WHERE username = 'your_user';

-- 若你希望把某些企业账号临时排查为求职者测试，可批量执行（按需）：
-- UPDATE sys_user SET `role` = 4 WHERE `role` = 3 AND username IN ('zhangsan', 'lisi');

-- 同步清理：如需恢复企业/管理员可再改回对应角色数值。
-- role: 1=管理员, 2=运营, 3=企业, 4=求职者
