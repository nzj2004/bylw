-- Repair seeded demo data after v1.3 process testing.
-- This script is non-destructive: it updates known demo rows only.

USE job_recruitment;
SET NAMES utf8mb4;

SELECT id INTO @alibaba_user_id FROM sys_user WHERE username = 'alibaba' LIMIT 1;
SELECT id INTO @tencent_user_id FROM sys_user WHERE username = 'tencent' LIMIT 1;
SELECT id INTO @baidu_user_id FROM sys_user WHERE username = 'baidu' LIMIT 1;
SELECT id INTO @zhangsan_user_id FROM sys_user WHERE username = 'zhangsan' LIMIT 1;
SELECT id INTO @lisi_user_id FROM sys_user WHERE username = 'lisi' LIMIT 1;
SELECT id INTO @wangwu_user_id FROM sys_user WHERE username = 'wangwu' LIMIT 1;
SELECT id INTO @zhouhao_user_id FROM sys_user WHERE username = 'zhouhao' LIMIT 1;

UPDATE sys_user SET real_name = '阿里巴巴HR' WHERE username = 'alibaba';
UPDATE sys_user SET real_name = '腾讯HR' WHERE username = 'tencent';
UPDATE sys_user SET real_name = '百度HR' WHERE username = 'baidu';
UPDATE sys_user SET real_name = '张三' WHERE username = 'zhangsan';
UPDATE sys_user SET real_name = '李四' WHERE username = 'lisi';
UPDATE sys_user SET real_name = '王五' WHERE username = 'wangwu';
UPDATE sys_user SET real_name = '周昊' WHERE username = 'zhouhao';

UPDATE company_info
SET company_name = '阿里巴巴集团',
    industry = '互联网/云计算',
    scale = '10000人以上',
    address = '杭州余杭区阿里巴巴西溪园区',
    description = '阿里巴巴集团是领先的互联网和云计算企业。',
    contact_name = '阿里巴巴HR'
WHERE user_id = @alibaba_user_id AND deleted = 0;

UPDATE company_info
SET company_name = '腾讯科技',
    industry = '互联网/游戏',
    scale = '10000人以上',
    address = '深圳南山区腾讯大厦',
    description = '腾讯科技提供互联网增值服务和数字内容服务。',
    contact_name = '腾讯HR'
WHERE user_id = @tencent_user_id AND deleted = 0;

UPDATE company_info
SET company_name = '百度在线',
    industry = '人工智能/搜索',
    scale = '10000人以上',
    address = '北京海淀区百度科技园',
    description = '百度在线专注于搜索、人工智能和云服务。',
    contact_name = '百度HR'
WHERE user_id = @baidu_user_id AND deleted = 0;

SELECT id INTO @alibaba_company_id FROM company_info WHERE user_id = @alibaba_user_id AND deleted = 0 LIMIT 1;
SELECT id INTO @tencent_company_id FROM company_info WHERE user_id = @tencent_user_id AND deleted = 0 LIMIT 1;
SELECT id INTO @baidu_company_id FROM company_info WHERE user_id = @baidu_user_id AND deleted = 0 LIMIT 1;

UPDATE resume
SET real_name = '张三',
    education = '本科',
    school = '浙江大学',
    major = '计算机科学与技术',
    work_experience = 'Java 后端开发工程师，参与电商订单系统开发。',
    project_exp = '电商订单系统重构项目。',
    self_eval = '学习能力强，沟通协作良好。',
    skills = 'Java、Spring Boot、MySQL、Redis',
    expected_city = '杭州',
    expected_salary_min = 15,
    expected_salary_max = 25
WHERE user_id = @zhangsan_user_id AND deleted = 0;

UPDATE resume
SET real_name = '李四',
    education = '本科',
    school = '深圳大学',
    major = '软件工程',
    work_experience = '前端开发工程师，负责企业管理系统前端开发。',
    project_exp = '企业管理系统项目。',
    self_eval = '关注用户体验，解决问题能力强。',
    skills = 'Vue、React、JavaScript、TypeScript',
    expected_city = '深圳',
    expected_salary_min = 12,
    expected_salary_max = 20
WHERE user_id = @lisi_user_id AND deleted = 0;

UPDATE resume
SET real_name = '王五',
    education = '硕士',
    school = '清华大学',
    major = '人工智能',
    work_experience = '算法工程师，负责推荐系统建模。',
    project_exp = '个性化推荐系统。',
    self_eval = '算法基础扎实，熟悉机器学习。',
    skills = 'Python、PyTorch、TensorFlow、机器学习',
    expected_city = '北京',
    expected_salary_min = 25,
    expected_salary_max = 40
WHERE user_id = @wangwu_user_id AND deleted = 0;

UPDATE resume
SET real_name = '周昊',
    education = '本科',
    school = '上海财经大学',
    major = '市场营销',
    work_experience = '金融科技公司市场运营，负责用户增长和活动转化。',
    project_exp = '银行获客增长项目，负责用户分层和转化漏斗优化。',
    self_eval = '沟通能力强，熟悉金融行业营销和数据分析。',
    skills = '市场调研、活动策划、SQL、Excel、用户增长',
    expected_city = '深圳',
    expected_salary_min = 12,
    expected_salary_max = 22
WHERE user_id = @zhouhao_user_id AND deleted = 0;

UPDATE job_info
SET title = 'Java 后端工程师',
    category = '计算机软件',
    salary_min = 15,
    salary_max = 25,
    work_city = '杭州',
    work_address = '阿里巴巴西溪园区',
    experience = '3-5年',
    education = '本科',
    job_desc = '负责核心业务系统后端开发。',
    requirements = '熟悉 Java、Spring Boot、MySQL、Redis。',
    welfare = '五险一金、年终奖、带薪年假'
WHERE company_id = @alibaba_company_id AND title IN ('Java Backend Engineer', 'Java 后端工程师') AND deleted = 0;

UPDATE job_info
SET title = '算法工程师',
    category = '人工智能',
    salary_min = 25,
    salary_max = 40,
    work_city = '北京',
    work_address = '阿里巴巴北京办公室',
    experience = '3-5年',
    education = '硕士',
    job_desc = '负责搜索和推荐算法研发。',
    requirements = '熟悉机器学习和深度学习算法。',
    welfare = '五险一金、年终奖、股票期权'
WHERE company_id = @alibaba_company_id AND title IN ('Algorithm Engineer', '算法工程师') AND deleted = 0;

UPDATE job_info
SET title = '市场营销经理(银行)',
    category = '银行',
    salary_min = 16,
    salary_max = 26,
    work_city = '深圳',
    work_address = '腾讯大厦',
    experience = '3-5年',
    education = '本科',
    job_desc = '负责金融行业客户增长和品牌营销。',
    requirements = '熟悉银行业务和市场活动策划。',
    welfare = '五险一金、年终奖、补充医疗'
WHERE company_id = @tencent_company_id AND title IN ('Market Manager (Banking)', '市场营销经理(银行)') AND deleted = 0;

UPDATE job_info
SET title = '品牌经理',
    category = '市场营销',
    salary_min = 15,
    salary_max = 28,
    work_city = '北京',
    work_address = '百度科技园',
    experience = '3-5年',
    education = '本科',
    job_desc = '负责品牌传播、内容策划和活动落地。',
    requirements = '具备品牌策略和跨团队协作经验。',
    welfare = '五险一金、年终奖、带薪年假'
WHERE company_id = @baidu_company_id AND title IN ('Brand Manager', '品牌经理') AND deleted = 0;

UPDATE job_info
SET title = '私人银行顾问',
    category = '银行',
    salary_min = 18,
    salary_max = 32,
    work_city = '北京',
    work_address = '金融街',
    experience = '3-5年',
    education = '本科',
    job_desc = '负责高净值客户关系维护和资产配置咨询。',
    requirements = '熟悉金融产品和客户关系维护。',
    welfare = '五险一金、绩效奖金、补充医疗'
WHERE company_id = @baidu_company_id AND title IN ('Private Banking Consultant', '私人银行顾问') AND deleted = 0;

UPDATE job_info
SET title = '行政专员',
    category = '企业服务',
    salary_min = 8,
    salary_max = 13,
    work_city = '北京',
    work_address = '百度科技园',
    experience = '1-3年',
    education = '本科',
    job_desc = '负责办公室行政、资产和会议支持。',
    requirements = '细致负责，沟通协调能力强。',
    welfare = '五险一金、带薪年假、节日福利'
WHERE company_id = @baidu_company_id AND title IN ('Administrative Specialist', '行政专员') AND deleted = 0;

UPDATE job_info
SET title = '区块链开发工程师',
    category = '计算机软件',
    salary_min = 20,
    salary_max = 35,
    work_city = '深圳',
    work_address = '腾讯大厦',
    experience = '3-5年',
    education = '本科',
    job_desc = '负责区块链底层服务和智能合约开发。',
    requirements = '熟悉 Go、Java、Solidity 之一。',
    welfare = '五险一金、年终奖、弹性工作'
WHERE company_id = @tencent_company_id AND title IN ('Blockchain Developer', '区块链开发工程师') AND deleted = 0;

SELECT id INTO @job_java FROM job_info WHERE title = 'Java 后端工程师' AND company_id = @alibaba_company_id AND deleted = 0 LIMIT 1;
SELECT id INTO @job_algorithm FROM job_info WHERE title = '算法工程师' AND company_id = @alibaba_company_id AND deleted = 0 LIMIT 1;
SELECT id INTO @job_market FROM job_info WHERE title = '市场营销经理(银行)' AND company_id = @tencent_company_id AND deleted = 0 LIMIT 1;
SELECT id INTO @job_brand FROM job_info WHERE title = '品牌经理' AND company_id = @baidu_company_id AND deleted = 0 LIMIT 1;
SELECT id INTO @job_private_bank FROM job_info WHERE title = '私人银行顾问' AND company_id = @baidu_company_id AND deleted = 0 LIMIT 1;
SELECT id INTO @job_admin FROM job_info WHERE title = '行政专员' AND company_id = @baidu_company_id AND deleted = 0 LIMIT 1;
SELECT id INTO @job_blockchain FROM job_info WHERE title = '区块链开发工程师' AND company_id = @tencent_company_id AND deleted = 0 LIMIT 1;

SELECT id INTO @app_zhou_market FROM application WHERE job_id = @job_market AND user_id = @zhouhao_user_id AND deleted = 0 LIMIT 1;
SELECT id INTO @app_zhou_brand FROM application WHERE job_id = @job_brand AND user_id = @zhouhao_user_id AND deleted = 0 LIMIT 1;
SELECT id INTO @app_zhou_private_bank FROM application WHERE job_id = @job_private_bank AND user_id = @zhouhao_user_id AND deleted = 0 LIMIT 1;
SELECT id INTO @app_zhou_admin FROM application WHERE job_id = @job_admin AND user_id = @zhouhao_user_id AND deleted = 0 LIMIT 1;
SELECT id INTO @app_zhou_blockchain FROM application WHERE job_id = @job_blockchain AND user_id = @zhouhao_user_id AND deleted = 0 LIMIT 1;
SELECT id INTO @app_zhou_algorithm FROM application WHERE job_id = @job_algorithm AND user_id = @zhouhao_user_id AND deleted = 0 LIMIT 1;

UPDATE application SET remark = NULL WHERE id = @app_zhou_market;
UPDATE application SET remark = '企业已发放 Offer，等待求职者响应' WHERE id = @app_zhou_brand;
UPDATE application SET remark = '求职者已接受 Offer' WHERE id = @app_zhou_private_bank;
UPDATE application SET remark = '已安排一面' WHERE id = @app_zhou_admin;
UPDATE application SET remark = '终面通过，待发 Offer' WHERE id = @app_zhou_blockchain;
UPDATE application SET remark = 'Offer 已过期' WHERE id = @app_zhou_algorithm;

DELETE FROM application_status_history
WHERE application_id IN (@app_zhou_market, @app_zhou_brand, @app_zhou_private_bank, @app_zhou_admin, @app_zhou_blockchain, @app_zhou_algorithm);

INSERT INTO application_status_history (application_id, old_status, new_status, changed_by, changed_role, action, remark, create_time, deleted)
VALUES
(@app_zhou_market, NULL, 0, @zhouhao_user_id, '求职者', 'APPLY_CREATED', '求职者投递职位', '2026-05-23 23:14:14', 0),
(@app_zhou_brand, NULL, 0, @zhouhao_user_id, '求职者', 'APPLY_CREATED', '求职者投递职位', '2026-05-09 13:45:46', 0),
(@app_zhou_brand, 0, 1, @baidu_user_id, '企业', 'RESUME_VIEWED', '企业查看简历', '2026-05-10 09:00:00', 0),
(@app_zhou_brand, 1, 6, @baidu_user_id, '企业', 'OFFER_SENT', '企业发送 Offer', '2026-05-20 18:00:00', 0),
(@app_zhou_private_bank, NULL, 0, @zhouhao_user_id, '求职者', 'APPLY_CREATED', '求职者投递职位', '2026-04-30 13:43:04', 0),
(@app_zhou_private_bank, 0, 6, @baidu_user_id, '企业', 'OFFER_SENT', '企业发送 Offer', '2026-05-10 10:00:00', 0),
(@app_zhou_private_bank, 6, 7, @zhouhao_user_id, '求职者', 'OFFER_ACCEPTED', '求职者接受 Offer', '2026-05-15 10:00:00', 0),
(@app_zhou_admin, NULL, 0, @zhouhao_user_id, '求职者', 'APPLY_CREATED', '求职者投递职位', '2026-04-30 13:42:58', 0),
(@app_zhou_admin, 0, 1, @baidu_user_id, '企业', 'RESUME_VIEWED', '企业查看简历', '2026-05-01 10:00:00', 0),
(@app_zhou_admin, 1, 4, @baidu_user_id, '企业', 'INTERVIEW_CREATED', '已安排一面', '2026-05-03 14:00:00', 0),
(@app_zhou_blockchain, NULL, 0, @zhouhao_user_id, '求职者', 'APPLY_CREATED', '求职者投递职位', '2026-04-30 13:42:54', 0),
(@app_zhou_blockchain, 0, 4, @tencent_user_id, '企业', 'INTERVIEW_CREATED', '企业安排面试', '2026-05-05 10:00:00', 0),
(@app_zhou_blockchain, 4, 10, @tencent_user_id, '企业', 'INTERVIEW_PASSED', '终面通过', '2026-05-08 17:30:00', 0),
(@app_zhou_algorithm, NULL, 0, @zhouhao_user_id, '求职者', 'APPLY_CREATED', '求职者投递职位', '2026-02-18 00:00:00', 0),
(@app_zhou_algorithm, 0, 6, @alibaba_user_id, '企业', 'OFFER_SENT', '企业发送 Offer', '2026-02-20 09:00:00', 0),
(@app_zhou_algorithm, 6, 11, NULL, '系统', 'OFFER_EXPIRED', 'Offer 超过有效期自动过期', '2026-03-01 00:00:00', 0);

UPDATE interview_rounds
SET interview_type = '线上',
    meeting_link = 'https://meeting.example.com/admin-1',
    contact_person = '百度HR',
    notes = '等待求职者确认'
WHERE application_id = @app_zhou_admin AND round_no = 1 AND deleted = 0;

UPDATE interview_rounds
SET interview_type = '线上',
    meeting_link = 'https://meeting.example.com/blockchain-1',
    contact_person = '腾讯HR',
    notes = '求职者已申请改期',
    reschedule_reason = '时间冲突，希望调整面试时间'
WHERE application_id = @app_zhou_blockchain AND round_no = 1 AND deleted = 0;

UPDATE interview_rounds
SET interview_type = '线下',
    location = '百度科技园 8F',
    contact_person = '百度HR',
    notes = '一面通过'
WHERE application_id = @app_zhou_private_bank AND round_no = 1 AND deleted = 0;

UPDATE offers
SET offer_title = '品牌经理 Offer',
    salary_min = 15,
    salary_max = 28,
    benefits = '五险一金、年终奖、带薪年假',
    work_location = '北京',
    offer_content = '请在有效期内确认是否接受该 Offer。'
WHERE application_id = @app_zhou_brand AND deleted = 0;

UPDATE offers
SET offer_title = '私人银行顾问 Offer',
    salary_min = 18,
    salary_max = 32,
    benefits = '五险一金、绩效奖金、补充医疗',
    work_location = '北京',
    offer_content = 'Offer 已接受。',
    response_comment = '接受，谢谢。'
WHERE application_id = @app_zhou_private_bank AND deleted = 0;

UPDATE offers
SET offer_title = '算法工程师 Offer',
    salary_min = 25,
    salary_max = 40,
    benefits = '五险一金、年终奖、股票期权',
    work_location = '北京',
    offer_content = 'Offer 已过期。',
    response_comment = '系统自动过期。'
WHERE application_id = @app_zhou_algorithm AND deleted = 0;

DELETE FROM user_notification
WHERE user_id IN (@zhouhao_user_id, @baidu_user_id, @tencent_user_id)
  AND business_id IN (@app_zhou_admin, @app_zhou_brand, @app_zhou_algorithm, @app_zhou_blockchain, @app_zhou_private_bank);

INSERT INTO user_notification (user_id, title, content, notification_type, business_type, business_id, read_status, read_time, create_time, deleted)
VALUES
(@zhouhao_user_id, '新的面试安排', '行政专员已安排第 1 轮面试，请确认或申请改期。', 'INTERVIEW', 'APPLICATION', @app_zhou_admin, 0, NULL, '2026-05-24 09:00:00', 0),
(@zhouhao_user_id, 'Offer 待确认', '品牌经理 Offer 正在等待你的响应。', 'APPLICATION', 'APPLICATION', @app_zhou_brand, 0, NULL, '2026-05-24 09:05:00', 0),
(@zhouhao_user_id, 'Offer 已过期', '算法工程师 Offer 已超过有效期。', 'APPLICATION', 'APPLICATION', @app_zhou_algorithm, 1, '2026-03-01 00:05:00', '2026-03-01 00:00:00', 0),
(@zhouhao_user_id, '改期申请已提交', '区块链开发工程师面试改期申请已提交，等待企业处理。', 'INTERVIEW', 'APPLICATION', @app_zhou_blockchain, 0, NULL, '2026-05-24 10:00:00', 0),
(@baidu_user_id, '求职者接受 Offer', '周昊已接受私人银行顾问 Offer。', 'OFFER', 'APPLICATION', @app_zhou_private_bank, 0, NULL, '2026-05-15 10:00:00', 0),
(@tencent_user_id, '求职者申请改期', '周昊申请调整区块链开发工程师面试时间。', 'INTERVIEW', 'APPLICATION', @app_zhou_blockchain, 0, NULL, '2026-05-24 10:00:00', 0);

UPDATE job_info j
SET apply_count = (
    SELECT COUNT(1)
    FROM application a
    WHERE a.job_id = j.id AND a.deleted = 0
)
WHERE j.deleted = 0;

SELECT 'zhouhao_notifications' AS item, COUNT(*) AS count
FROM user_notification
WHERE user_id = @zhouhao_user_id AND deleted = 0
UNION ALL
SELECT 'zhouhao_unread', COUNT(*)
FROM user_notification
WHERE user_id = @zhouhao_user_id AND read_status = 0 AND deleted = 0
UNION ALL
SELECT 'zhouhao_histories', COUNT(*)
FROM application_status_history h
INNER JOIN application a ON a.id = h.application_id
WHERE a.user_id = @zhouhao_user_id AND h.deleted = 0
UNION ALL
SELECT 'bad_salary_rows', COUNT(*)
FROM job_info
WHERE deleted = 0 AND (salary_min >= 1000 OR salary_max >= 1000);
