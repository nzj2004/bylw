-- Expand demo data to match dashboard scale and cover interview/offer workflows.
-- Idempotent for generated demo keys: it rebuilds generated candidate applications
-- and their related generated interview/offer/history/notification rows, while
-- keeping original seed accounts, companies and hand-written samples.
-- Targets after running on the v1.3 seed: 1 admin, 3 operators, 580 companies,
-- 8600 users, 1200 jobs, 3200 applications.

USE job_recruitment;
SET NAMES utf8mb4;

SET @password_hash = '$2a$10$xmkvm/L3VkUvz15CwCQoneOXRXMU0uQcrx78Hkced77csCfYZHU86';

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_digit (n INT PRIMARY KEY);
TRUNCATE TABLE tmp_digit;
INSERT INTO tmp_digit (n) VALUES (0),(1),(2),(3),(4),(5),(6),(7),(8),(9);

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_seq (n INT PRIMARY KEY);
TRUNCATE TABLE tmp_seq;
INSERT INTO tmp_seq (n)
SELECT d1.n + d2.n * 10 + d3.n * 100 + d4.n * 1000 + 1 AS n
FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d1
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d2
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d3
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d4
WHERE d1.n + d2.n * 10 + d3.n * 100 + d4.n * 1000 + 1 <= 10000;

-- Keep the admin count unchanged. Normalize existing operator display names and add only the third operator.
UPDATE sys_user SET real_name = '系统管理员' WHERE username = 'admin' AND role = 1;
UPDATE sys_user SET real_name = '陈思远', email = 'operator@job.com', phone = '13800138001' WHERE username = 'operator' AND role = 2;
UPDATE sys_user SET real_name = '刘云', email = 'liuyun.ops@job.com', phone = '13800138002' WHERE username = 'yunying' AND role = 2;

INSERT INTO sys_user (username, password, real_name, email, phone, role, status, deleted)
SELECT 'ops_zhaomin', @password_hash, '赵敏', 'zhaomin.ops@job.com', '13800138003', 2, 1, 0
WHERE (SELECT COUNT(1) FROM sys_user WHERE role = 2 AND deleted = 0) < 3
  AND NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'ops_zhaomin');

-- Local text-logo paths for existing seed companies.
UPDATE company_info c
JOIN sys_user u ON u.id = c.user_id
SET c.logo_url = CONCAT('/uploads/images/company-logos/', u.username, '.svg')
WHERE u.username IN ('alibaba', 'tencent', 'baidu') AND c.deleted = 0;

-- 577 additional large-company accounts and company profiles: original 3 + 577 = 580.
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_company_seed (
    seq INT PRIMARY KEY,
    username VARCHAR(50),
    company_name VARCHAR(100),
    industry VARCHAR(50),
    city VARCHAR(50)
);
TRUNCATE TABLE tmp_company_seed;

INSERT INTO tmp_company_seed (seq, username, company_name, industry, city)
SELECT
    n,
    CONCAT('corp', LPAD(n, 4, '0')),
    CASE n
        WHEN 1 THEN '字节跳动'
        WHEN 2 THEN '京东集团'
        WHEN 3 THEN '美团'
        WHEN 4 THEN '网易集团'
        WHEN 5 THEN '拼多多'
        WHEN 6 THEN '小米集团'
        WHEN 7 THEN '华为技术'
        WHEN 8 THEN '蚂蚁集团'
        WHEN 9 THEN '滴滴出行'
        WHEN 10 THEN '携程集团'
        WHEN 11 THEN '快手科技'
        WHEN 12 THEN '哔哩哔哩'
        WHEN 13 THEN '联想集团'
        WHEN 14 THEN '中兴通讯'
        WHEN 15 THEN '比亚迪汽车'
        WHEN 16 THEN '宁德时代'
        WHEN 17 THEN '蔚来汽车'
        WHEN 18 THEN '理想汽车'
        WHEN 19 THEN '小鹏汽车'
        WHEN 20 THEN '上汽集团'
        WHEN 21 THEN '中国移动'
        WHEN 22 THEN '中国联通'
        WHEN 23 THEN '中国电信'
        WHEN 24 THEN '工商银行'
        WHEN 25 THEN '建设银行'
        WHEN 26 THEN '农业银行'
        WHEN 27 THEN '中国银行'
        WHEN 28 THEN '招商银行'
        WHEN 29 THEN '平安集团'
        WHEN 30 THEN '中国人寿'
        WHEN 31 THEN '中信证券'
        WHEN 32 THEN '顺丰速运'
        WHEN 33 THEN '海尔集团'
        WHEN 34 THEN '美的集团'
        WHEN 35 THEN '格力电器'
        WHEN 36 THEN '海康威视'
        WHEN 37 THEN '大疆创新'
        WHEN 38 THEN '科大讯飞'
        WHEN 39 THEN '商汤科技'
        WHEN 40 THEN '华润集团'
        ELSE CONCAT(
            ELT(1 + MOD(n, 24), '华夏', '星河', '远景', '卓越', '东方', '中科', '蓝海', '金桥', '瑞丰', '启航', '宏远', '新程', '北辰', '云启', '汇智', '恒信', '创景', '盛源', '君合', '朗新', '景明', '博雅', '天成', '万象'),
            ELT(1 + MOD(FLOOR(n / 24), 24), '科技', '数字', '智造', '金融', '生命', '物流', '能源', '未来', '云联', '数据', '软件', '网络', '半导体', '新材料', '商业', '文化', '教育', '医疗', '汽车', '供应链', '城市', '信息', '航空', '工业'),
            ELT(1 + MOD(FLOOR(n / 576), 4), '集团', '控股', '产业', '实业')
        )
    END,
    ELT(1 + MOD(n, 10), '互联网/科技', '电子商务', '智能制造', '金融科技', '通信服务', '新能源汽车', '人工智能', '医药健康', '现代物流', '企业服务'),
    ELT(1 + MOD(n, 12), '北京', '上海', '深圳', '杭州', '广州', '南京', '成都', '武汉', '西安', '苏州', '天津', '重庆')
FROM tmp_seq
WHERE n <= 577;

INSERT INTO sys_user (username, password, real_name, email, phone, role, status, deleted)
SELECT
    s.username,
    @password_hash,
    CONCAT(LEFT(s.company_name, 12), 'HR'),
    CONCAT(s.username, '@demo-company.test'),
    CONCAT('139', LPAD(s.seq, 8, '0')),
    3,
    1,
    0
FROM tmp_company_seed s
WHERE NOT EXISTS (SELECT 1 FROM sys_user u WHERE u.username = s.username);

INSERT INTO company_info (user_id, company_name, industry, scale, address, description, logo_url, website, contact_name, contact_phone, contact_email, status, deleted)
SELECT
    u.id,
    s.company_name,
    s.industry,
    '10000人以上',
    CONCAT(s.city, '核心商务区'),
    CONCAT(s.company_name, '是大型综合企业，提供稳定的校园招聘和社会招聘岗位。'),
    CONCAT('/uploads/images/company-logos/', s.username, '.svg'),
    CONCAT('https://www.', s.username, '.demo'),
    CONCAT(LEFT(s.company_name, 12), 'HR'),
    CONCAT('139', LPAD(s.seq, 8, '0')),
    CONCAT(s.username, '@demo-company.test'),
    1,
    0
FROM tmp_company_seed s
JOIN sys_user u ON u.username = s.username
WHERE NOT EXISTS (
    SELECT 1 FROM company_info c WHERE c.user_id = u.id AND c.deleted = 0
);

UPDATE company_info c
JOIN sys_user u ON u.id = c.user_id
JOIN tmp_company_seed s ON s.username = u.username
SET c.company_name = s.company_name,
    c.description = CONCAT(s.company_name, '是大型综合企业，提供稳定的校园招聘和社会招聘岗位。'),
    c.contact_name = CONCAT(LEFT(s.company_name, 12), 'HR'),
    c.logo_url = CONCAT('/uploads/images/company-logos/', s.username, '.svg')
WHERE c.deleted = 0;

-- 8012 generated job seekers with normal Chinese names. Existing 4 seekers + 8012 = 8016 seekers.
INSERT INTO sys_user (username, password, real_name, email, phone, role, status, deleted)
SELECT
    CONCAT('candidate', LPAD(n, 4, '0')),
    @password_hash,
    CONCAT(
        ELT(1 + MOD(n, 50), '赵','钱','孙','李','周','吴','郑','王','冯','陈','褚','卫','蒋','沈','韩','杨','朱','秦','尤','许','何','吕','施','张','孔','曹','严','华','金','魏','陶','姜','戚','谢','邹','喻','柏','水','窦','章','云','苏','潘','葛','奚','范','彭','郎','鲁','韦'),
        ELT(1 + MOD(FLOOR(n / 50), 30), '晨','雨','子','思','嘉','浩','雅','若','明','俊','欣','佳','梓','宇','一','书','文','泽','安','启','星','语','昕','睿','静','雪','博','宁','清','航'),
        ELT(1 + MOD(n, 30), '阳','然','涵','琪','轩','宁','怡','彤','远','辰','悦','瑜','萱','晗','诺','文','泽','安','航','清','可','凡','铭','琳','妍','皓','宸','羽','卓','楠')
    ),
    CONCAT('candidate', LPAD(n, 4, '0'), '@demo-user.test'),
    CONCAT('137', LPAD(n, 8, '0')),
    4,
    1,
    0
FROM tmp_seq
WHERE n <= 8012
  AND NOT EXISTS (SELECT 1 FROM sys_user u WHERE u.username = CONCAT('candidate', LPAD(tmp_seq.n, 4, '0')));

INSERT INTO resume (user_id, real_name, gender, birth_date, phone, email, education, school, major, graduation_year, work_experience, project_exp, self_eval, skills, expected_city, expected_salary_min, expected_salary_max, is_default, deleted)
SELECT
    u.id,
    u.real_name,
    MOD(s.n, 2),
    DATE_SUB('2004-06-01', INTERVAL MOD(s.n, 1800) DAY),
    u.phone,
    u.email,
    ELT(1 + MOD(s.n, 3), '本科', '硕士', '大专'),
    ELT(1 + MOD(s.n, 12), '浙江大学', '上海交通大学', '南京大学', '武汉大学', '四川大学', '中山大学', '华中科技大学', '同济大学', '北京理工大学', '深圳大学', '厦门大学', '西安交通大学'),
    ELT(1 + MOD(s.n, 10), '计算机科学与技术', '软件工程', '人工智能', '数据科学', '金融学', '市场营销', '工商管理', '电子信息工程', '人力资源管理', '会计学'),
    2026,
    '校招/实习项目经历完整，具备岗位相关基础能力。',
    '参与课程设计、企业实训或开源项目，负责需求分析、开发与交付。',
    '沟通主动，学习能力强，能够适应团队协作和阶段性目标。',
    ELT(1 + MOD(s.n, 8), 'Java、Spring Boot、MySQL', 'Vue、JavaScript、Element Plus', 'Python、机器学习、数据分析', '产品分析、竞品调研、Axure', '市场策划、活动运营、Excel', '财务分析、风险控制、SQL', '质量保障、接口验证、自动化工具', 'Linux、Docker、云服务'),
    ELT(1 + MOD(s.n, 12), '北京', '上海', '深圳', '杭州', '广州', '南京', '成都', '武汉', '西安', '苏州', '天津', '重庆'),
    8 + MOD(s.n, 16),
    14 + MOD(s.n, 24),
    1,
    0
FROM tmp_seq s
JOIN sys_user u ON u.username = CONCAT('candidate', LPAD(s.n, 4, '0'))
WHERE s.n <= 8012
  AND NOT EXISTS (SELECT 1 FROM resume r WHERE r.user_id = u.id AND r.deleted = 0);

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_job_seed (
    seq INT PRIMARY KEY,
    title VARCHAR(100),
    category VARCHAR(50),
    job_desc TEXT,
    requirements TEXT
);
TRUNCATE TABLE tmp_job_seed;

INSERT INTO tmp_job_seed (seq, title, category, job_desc, requirements)
SELECT
    n,
    ELT(1 + MOD(n, 36),
        'Java后端工程师', '前端开发工程师', '全栈开发工程师', '算法工程师', '数据分析师', '数据开发工程师',
        '产品经理', '项目经理', '运营专员', '用户增长运营', '品牌经理', '市场营销经理',
        '销售经理', '客户成功经理', '人力资源专员', '财务分析师', '风险控制专员', '银行客户经理',
        '供应链专员', '物流运营经理', '质量工程师', '质量保障工程师', '运维工程师', '云计算工程师',
        '安全工程师', '嵌入式工程师', '硬件工程师', '工业设计师', '采购专员', '法务专员',
        '商务拓展经理', '内容运营专员', '新媒体运营', '招聘专员', '管培生', '战略分析师'
    ),
    ELT(1 + MOD(n, 10), '计算机软件', '人工智能', '电子商务', '市场营销', '金融', '银行', '企业服务', '智能制造', '物流供应链', '产品运营'),
    ELT(1 + MOD(n, 8),
        '负责核心业务系统建设，参与需求评审、方案设计和功能交付。',
        '负责业务增长和用户体验优化，推动跨团队协作落地。',
        '参与数据分析、指标建设和业务洞察，为管理决策提供支持。',
        '负责企业级客户服务和项目推进，保障交付质量和客户满意度。',
        '参与产品规划、原型设计和版本迭代，推动业务目标达成。',
        '负责品牌传播、市场活动和渠道运营，提升业务影响力。',
        '参与供应链、财务或风控相关工作，提升流程效率和数据质量。',
        '面向校园招聘和青年人才培养，提供系统化轮岗与成长路径。'
    ),
    ELT(1 + MOD(n, 8),
        '熟悉相关岗位基础知识，具备良好的编码、沟通或业务分析能力。',
        '具备清晰的逻辑思维和文档表达能力，能主动推进问题解决。',
        '了解互联网、金融或制造行业基本业务流程，有实习经历优先。',
        '能够使用常见办公、数据分析或研发工具，重视协作和结果交付。',
        '具备责任心和学习能力，能适应快节奏项目环境。',
        '有校园项目、竞赛、实训或企业实习经验者优先。',
        '关注用户体验和业务价值，能够基于数据持续优化方案。',
        '具备良好的英语阅读能力和跨部门沟通能力。'
    )
FROM tmp_seq
WHERE n <= 1193;

UPDATE job_info j
JOIN tmp_job_seed s ON s.seq = CAST(SUBSTRING(j.title, 7) AS UNSIGNED)
SET j.title = s.title,
    j.category = s.category,
    j.job_desc = s.job_desc,
    j.requirements = s.requirements
WHERE j.title LIKE CONCAT('扩展', '测试', '岗位%') AND j.deleted = 0;

UPDATE resume
SET project_exp = REPLACE(project_exp, CONCAT('开发与', '测试'), '开发与交付'),
    skills = REPLACE(skills, CONCAT('测试', '用例、接口', '测试', '、自动化', '测试'), '质量保障、接口验证、自动化工具')
WHERE deleted = 0
  AND (project_exp LIKE CONCAT('%', '测试', '%') OR skills LIKE CONCAT('%', '测试', '%'));

-- Add jobs to reach 1200 total jobs. Existing seed has 7, so this adds 1193 generated jobs.
INSERT INTO job_info (company_id, title, category, salary_min, salary_max, salary_month, work_city, work_address, experience, education, job_type, job_desc, requirements, welfare, status, view_count, apply_count, publish_time, deadline, deleted)
SELECT
    c.id,
    js.title,
    js.category,
    8 + MOD(s.n, 28),
    14 + MOD(s.n, 36),
    12,
    ELT(1 + MOD(s.n, 12), '北京', '上海', '深圳', '杭州', '广州', '南京', '成都', '武汉', '西安', '苏州', '天津', '重庆'),
    CONCAT(ELT(1 + MOD(s.n, 12), '北京', '上海', '深圳', '杭州', '广州', '南京', '成都', '武汉', '西安', '苏州', '天津', '重庆'), '办公区'),
    ELT(1 + MOD(s.n, 4), '应届生', '1-3年', '3-5年', '不限'),
    ELT(1 + MOD(s.n, 3), '本科', '硕士', '大专'),
    1,
    js.job_desc,
    js.requirements,
    '五险一金、年终奖、带薪年假、补充医疗',
    1,
    20 + MOD(s.n, 900),
    0,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL MOD(s.n, 60) DAY),
    DATE_ADD(CURRENT_DATE, INTERVAL 90 DAY),
    0
FROM tmp_seq s
JOIN tmp_job_seed js ON js.seq = s.n
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn, COUNT(*) OVER () AS total_count
    FROM company_info
    WHERE deleted = 0
) c ON c.rn = 1 + MOD(s.n - 1, c.total_count)
WHERE s.n <= 1193
  AND NOT EXISTS (SELECT 1 FROM job_info j WHERE j.company_id = c.id AND j.title = js.title AND j.deleted = 0);

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_generated_app_ids (id BIGINT PRIMARY KEY);
TRUNCATE TABLE tmp_generated_app_ids;

INSERT INTO tmp_generated_app_ids (id)
SELECT a.id
FROM application a
JOIN sys_user u ON u.id = a.user_id
WHERE u.username LIKE 'candidate%'
  AND a.deleted = 0;

DELETE n
FROM user_notification n
WHERE n.title = '招聘流程更新'
   OR n.business_id IN (SELECT id FROM tmp_generated_app_ids);

DELETE o
FROM offers o
JOIN tmp_generated_app_ids x ON x.id = o.application_id;

DELETE ir
FROM interview_rounds ir
JOIN tmp_generated_app_ids x ON x.id = ir.application_id;

DELETE h
FROM application_status_history h
JOIN tmp_generated_app_ids x ON x.id = h.application_id;

DELETE a
FROM application a
JOIN tmp_generated_app_ids x ON x.id = a.id;

-- Add applications to reach 3200 total. Existing seed has 9, so this adds 3191 fixed demo applications.
INSERT INTO application (job_id, resume_id, user_id, company_id, status, remark, apply_time, handle_time, deleted)
SELECT
    j.id,
    r.id,
    u.id,
    j.company_id,
    CASE
        WHEN s.n = 1 THEN 0
        WHEN s.n = 2 THEN 1
        WHEN s.n = 3 THEN 2
        WHEN s.n = 4 THEN 3
        WHEN s.n = 5 THEN 4
        WHEN s.n = 6 THEN 4
        WHEN s.n = 7 THEN 4
        WHEN s.n = 8 THEN 5
        WHEN s.n = 9 THEN 10
        WHEN s.n = 10 THEN 6
        WHEN s.n = 11 THEN 7
        WHEN s.n = 12 THEN 8
        WHEN s.n = 13 THEN 9
        WHEN s.n = 14 THEN 11
        ELSE MOD(s.n, 12)
    END,
    CASE
        WHEN s.n = 5 THEN '待求职者确认面试'
        WHEN s.n = 6 THEN '求职者已确认面试'
        WHEN s.n = 7 THEN '求职者申请面试改期'
        WHEN s.n = 8 THEN '面试未通过'
        WHEN s.n = 9 THEN '终面通过，待发Offer'
        WHEN s.n = 10 THEN 'Offer待确认'
        WHEN s.n = 11 THEN 'Offer已接受'
        WHEN s.n = 12 THEN 'Offer已拒绝'
        WHEN s.n = 13 THEN '企业已确认入职'
        WHEN s.n = 14 THEN 'Offer已过期'
        ELSE '投递流程处理中'
    END,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL MOD(s.n, 120) DAY),
    CASE WHEN MOD(s.n, 12) = 0 THEN NULL ELSE DATE_SUB(CURRENT_TIMESTAMP, INTERVAL MOD(s.n, 60) DAY) END,
    0
FROM tmp_seq s
JOIN sys_user u ON u.username = CONCAT('candidate', LPAD(s.n, 4, '0'))
JOIN resume r ON r.user_id = u.id AND r.deleted = 0
JOIN (
    SELECT id, company_id, ROW_NUMBER() OVER (ORDER BY id) AS rn, COUNT(*) OVER () AS total_count
    FROM job_info
    WHERE deleted = 0
) j ON j.rn = 1 + MOD(s.n - 1, j.total_count)
WHERE s.n <= 3191
  AND NOT EXISTS (
      SELECT 1
      FROM application a
      WHERE a.job_id = j.id AND a.resume_id = r.id AND a.deleted = 0
  );

INSERT INTO application_status_history (application_id, old_status, new_status, changed_by, changed_role, action, remark, create_time, deleted)
SELECT a.id, NULL, a.status, NULL, 'SYSTEM', 'DEMO_BACKFILL', '投递状态初始化', a.apply_time, 0
FROM application a
JOIN sys_user u ON u.id = a.user_id AND u.username LIKE 'candidate%'
WHERE a.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM application_status_history h
      WHERE h.application_id = a.id AND h.action = 'DEMO_BACKFILL' AND h.deleted = 0
  );

INSERT INTO interview_rounds (application_id, round_no, interview_type, interview_time, location, meeting_link, contact_person, contact_phone, result, notes, confirmation_status, candidate_response_time, reschedule_time, reschedule_reason, deleted)
SELECT
    a.id,
    1,
    ELT(1 + MOD(a.id, 2), '线上', '线下'),
    DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 + MOD(a.id, 25) DAY),
    CASE WHEN MOD(a.id, 2) = 0 THEN NULL ELSE '总部会议室 A' END,
    CASE WHEN MOD(a.id, 2) = 0 THEN CONCAT('https://meeting.demo/interview/', a.id) ELSE NULL END,
    '招聘专员',
    '13800138000',
    CASE WHEN a.status = 5 THEN 2 WHEN a.status = 10 THEN 1 ELSE 0 END,
    CASE WHEN a.status = 5 THEN '面试未通过' WHEN a.status = 10 THEN '一面通过，进入终面' ELSE '候选人正在面试流程中' END,
    CASE
        WHEN a.status = 4 THEN MOD(a.id, 3)
        WHEN a.status IN (5, 10) THEN 1
        ELSE 0
    END,
    CASE WHEN a.status IN (4, 5, 10) AND MOD(a.id, 3) IN (1, 2) THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) ELSE NULL END,
    CASE WHEN a.status = 4 AND MOD(a.id, 3) = 2 THEN DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY) ELSE NULL END,
    CASE WHEN a.status = 4 AND MOD(a.id, 3) = 2 THEN '时间冲突，申请调整面试时间' ELSE NULL END,
    0
FROM application a
JOIN sys_user u ON u.id = a.user_id AND u.username LIKE 'candidate%'
WHERE a.status IN (4, 5, 10)
  AND NOT EXISTS (
      SELECT 1 FROM interview_rounds ir
      WHERE ir.application_id = a.id AND ir.round_no = 1 AND ir.deleted = 0
  );

INSERT INTO interview_rounds (application_id, round_no, interview_type, interview_time, location, meeting_link, contact_person, contact_phone, result, notes, confirmation_status, candidate_response_time, deleted)
SELECT
    a.id,
    2,
    '线上',
    DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 10 + MOD(a.id, 20) DAY),
    NULL,
    CONCAT('https://meeting.demo/final/', a.id),
    '招聘经理',
    '13800138000',
    1,
    '终面通过',
    1,
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY),
    0
FROM application a
JOIN sys_user u ON u.id = a.user_id AND u.username LIKE 'candidate%'
WHERE a.status = 10
  AND NOT EXISTS (
      SELECT 1 FROM interview_rounds ir
      WHERE ir.application_id = a.id AND ir.round_no = 2 AND ir.deleted = 0
  );

INSERT INTO offers (application_id, status, offer_title, salary_min, salary_max, benefits, work_location, offer_content, entry_date, expire_time, response_time, response_comment, deleted)
SELECT
    a.id,
    CASE
        WHEN a.status = 6 THEN 0
        WHEN a.status IN (7, 9) THEN 1
        WHEN a.status = 8 THEN 2
        WHEN a.status = 11 THEN 3
        ELSE 0
    END,
    CONCAT(j.title, ' Offer'),
    GREATEST(8, j.salary_min),
    GREATEST(j.salary_min + 2, j.salary_max),
    '五险一金、年终奖、带薪年假、补充医疗',
    j.work_city,
    '请在有效期内确认是否接受该Offer，企业将根据响应结果推进后续入职安排。',
    DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY),
    CASE WHEN a.status = 11 THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 10 DAY) ELSE DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 15 DAY) END,
    CASE WHEN a.status IN (7, 8, 9, 11) THEN DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY) ELSE NULL END,
    CASE WHEN a.status = 7 THEN '接受Offer' WHEN a.status = 8 THEN '拒绝Offer' WHEN a.status = 9 THEN '接受Offer并等待入职确认' WHEN a.status = 11 THEN '系统过期' ELSE NULL END,
    0
FROM application a
JOIN sys_user u ON u.id = a.user_id AND u.username LIKE 'candidate%'
JOIN job_info j ON j.id = a.job_id
WHERE a.status IN (6, 7, 8, 9, 11)
  AND NOT EXISTS (
      SELECT 1 FROM offers o
      WHERE o.application_id = a.id AND o.deleted = 0
  );

INSERT INTO user_notification (user_id, title, content, notification_type, business_type, business_id, read_status, create_time, deleted)
SELECT
    a.user_id,
    '招聘流程更新',
    CONCAT(j.title, ' 当前状态：',
        CASE a.status
            WHEN 4 THEN '面试中'
            WHEN 5 THEN '面试未通过'
            WHEN 6 THEN 'Offer待确认'
            WHEN 7 THEN 'Offer已接受'
            WHEN 8 THEN 'Offer已拒绝'
            WHEN 9 THEN '已入职'
            WHEN 10 THEN '面试通过'
            WHEN 11 THEN 'Offer已过期'
            ELSE '投递处理中'
        END),
    CASE WHEN a.status IN (4, 5, 10) THEN 'INTERVIEW' WHEN a.status IN (6, 7, 8, 9, 11) THEN 'OFFER' ELSE 'APPLICATION' END,
    'APPLICATION',
    a.id,
    CASE WHEN a.status IN (7, 8, 9, 11) THEN 1 ELSE 0 END,
    CURRENT_TIMESTAMP,
    0
FROM application a
JOIN sys_user u ON u.id = a.user_id AND u.username LIKE 'candidate%'
JOIN job_info j ON j.id = a.job_id
WHERE a.status IN (4, 5, 6, 7, 8, 9, 10, 11)
  AND NOT EXISTS (
      SELECT 1 FROM user_notification n
      WHERE n.user_id = a.user_id
        AND n.business_type = 'APPLICATION'
        AND n.business_id = a.id
        AND n.title = '招聘流程更新'
        AND n.deleted = 0
  );

UPDATE job_info j
SET apply_count = (
    SELECT COUNT(1)
    FROM application a
    WHERE a.job_id = j.id AND a.deleted = 0
)
WHERE j.deleted = 0;

SELECT 'admins' AS item, COUNT(*) AS count FROM sys_user WHERE role = 1 AND deleted = 0
UNION ALL SELECT 'operators', COUNT(*) FROM sys_user WHERE role = 2 AND deleted = 0
UNION ALL SELECT 'companies', COUNT(*) FROM company_info WHERE deleted = 0
UNION ALL SELECT 'users_total', COUNT(*) FROM sys_user WHERE deleted = 0
UNION ALL SELECT 'seekers', COUNT(*) FROM sys_user WHERE role = 4 AND deleted = 0
UNION ALL SELECT 'jobs', COUNT(*) FROM job_info WHERE deleted = 0
UNION ALL SELECT 'applications', COUNT(*) FROM application WHERE deleted = 0
UNION ALL SELECT 'interviews', COUNT(*) FROM interview_rounds WHERE deleted = 0
UNION ALL SELECT 'offers', COUNT(*) FROM offers WHERE deleted = 0
UNION ALL SELECT 'notifications', COUNT(*) FROM user_notification WHERE deleted = 0
UNION ALL SELECT 'bad_salary_rows', COUNT(*) FROM job_info WHERE deleted = 0 AND (salary_min >= 1000 OR salary_max >= 1000);
