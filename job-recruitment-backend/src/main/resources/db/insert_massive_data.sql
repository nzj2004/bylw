-- 批量插入约1000条测试数据
USE job_recruitment;

-- 设置变量
SET @i = 1;
SET @company_start_id = 3;
SET @job_seeker_start_id = 6;

-- ============================================
-- 1. 批量生成企业用户 (50家企业)
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS InsertCompanies$$
CREATE PROCEDURE InsertCompanies()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE company_names VARCHAR(255);
    DECLARE industries VARCHAR(50);
    DECLARE cities VARCHAR(20);
    
    WHILE i <= 50 DO
        SET company_names = ELT(i % 10 + 1, 
            '华为技术有限公司', '小米科技有限公司', '字节跳动', '美团点评', '京东集团',
            '网易公司', '滴滴出行', '拼多多', '快手科技', '新浪微博');
        SET industries = ELT(i % 8 + 1, '互联网/电子商务', '计算机软件', '通信/电信', '移动互联网', 
            '人工智能', '大数据', '云计算', '物联网');
        SET cities = ELT(i % 6 + 1, '北京', '上海', '深圳', '杭州', '广州', '成都');
        
        -- 插入企业用户
        INSERT INTO sys_user (username, password, real_name, email, phone, role, status) 
        VALUES (
            CONCAT('company', i),
            '$2a$10$xmkvm/L3VkUvz15CwCQoneOXRXMU0uQcrx78Hkced77csCfYZHU86',
            CONCAT(company_names, 'HR'),
            CONCAT('hr', i, '@company.com'),
            CONCAT('138', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
            3,
            1
        );
        
        -- 获取刚插入的用户ID
        SET @user_id = LAST_INSERT_ID();
        
        -- 插入企业信息
        INSERT INTO company_info (user_id, company_name, industry, scale, address, description, 
            logo_url, website, contact_name, contact_phone, contact_email, status)
        VALUES (
            @user_id,
            CONCAT(company_names, '-', i, '分公司'),
            industries,
            ELT(i % 4 + 1, '100-499人', '500-999人', '1000-9999人', '10000人以上'),
            CONCAT(cities, '市高新区科技园', i, '号'),
            CONCAT(company_names, '是一家专注于', industries, '的高新技术企业，致力于为客户提供优质的产品和服务。'),
            CONCAT('https://logo.company', i, '.com'),
            CONCAT('https://www.company', i, '.com'),
            CONCAT('张经理', i),
            CONCAT('138', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
            CONCAT('hr', i, '@company.com'),
            1
        );
        
        SET i = i + 1;
    END WHILE;
END$$

-- ============================================
-- 2. 批量生成求职者 (200名求职者)
-- ============================================
DROP PROCEDURE IF EXISTS InsertJobSeekers$$
CREATE PROCEDURE InsertJobSeekers()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE first_names VARCHAR(50);
    DECLARE last_names VARCHAR(50);
    DECLARE educations VARCHAR(20);
    DECLARE schools VARCHAR(50);
    DECLARE majors VARCHAR(50);
    DECLARE cities VARCHAR(20);
    
    SET first_names = '伟芳娜敏静丽强磊军洋勇艳杰娟涛明超秀霞平刚桂';
    SET last_names = '王李张刘陈杨黄赵周吴徐孙马朱胡郭何林罗高郑';
    
    WHILE i <= 200 DO
        SET educations = ELT(i % 4 + 1, '大专', '本科', '硕士', '博士');
        SET schools = ELT(i % 10 + 1, '清华大学', '北京大学', '浙江大学', '复旦大学', '上海交通大学',
            '南京大学', '武汉大学', '中山大学', '四川大学', '华中科技大学');
        SET majors = ELT(i % 10 + 1, '计算机科学与技术', '软件工程', '电子信息工程', '自动化',
            '数学与应用数学', '物理学', '工商管理', '经济学', '法学', '英语');
        SET cities = ELT(i % 6 + 1, '北京', '上海', '深圳', '杭州', '广州', '成都');
        
        -- 插入求职者用户
        INSERT INTO sys_user (username, password, real_name, email, phone, role, status)
        VALUES (
            CONCAT('seeker', i),
            '$2a$10$xmkvm/L3VkUvz15CwCQoneOXRXMU0uQcrx78Hkced77csCfYZHU86',
            CONCAT(SUBSTRING(last_names, (i % 10) * 2 + 1, 2), SUBSTRING(first_names, (i % 20) * 2 + 1, 2)),
            CONCAT('seeker', i, '@qq.com'),
            CONCAT('139', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
            4,
            1
        );
        
        -- 获取刚插入的用户ID
        SET @user_id = LAST_INSERT_ID();
        
        -- 插入简历
        INSERT INTO resume (user_id, real_name, gender, birth_date, phone, email, education, school, major,
            graduation_year, work_experience, project_exp, self_eval, skills, expected_city,
            expected_salary_min, expected_salary_max, is_default)
        VALUES (
            @user_id,
            CONCAT(SUBSTRING(last_names, (i % 10) * 2 + 1, 2), SUBSTRING(first_names, (i % 20) * 2 + 1, 2)),
            i % 2,
            DATE_SUB(CURDATE(), INTERVAL (22 + i % 8) YEAR),
            CONCAT('139', LPAD(FLOOR(RAND() * 100000000), 8, '0')),
            CONCAT('seeker', i, '@qq.com'),
            educations,
            schools,
            majors,
            2020 + i % 5,
            CONCAT('2020.07-2023.06 某互联网公司 ', ELT(i % 5 + 1, 'Java开发', '前端开发', '产品经理', '测试工程师', '运维工程师'), '\n负责核心业务系统开发和维护'),
            CONCAT('企业管理系统项目\n使用', ELT(i % 3 + 1, 'Spring Boot', 'Vue3', 'React'), '开发，实现', ELT(i % 4 + 1, '用户管理', '订单系统', '数据分析', '消息推送'), '功能'),
            '工作认真负责，学习能力强，具备良好的团队协作精神和沟通能力',
            ELT(i % 8 + 1, 'Java、Spring Boot、MySQL', 'Vue、React、JavaScript', 'Python、数据分析', '产品规划、需求分析', 
                'Linux、Docker、K8s', 'UI设计、Figma、Sketch', '测试用例、自动化测试', '项目管理、团队协作'),
            cities,
            8000 + (i % 20) * 1000,
            15000 + (i % 30) * 1000,
            1
        );
        
        SET i = i + 1;
    END WHILE;
END$$

-- ============================================
-- 3. 批量生成职位 (300个职位)
-- ============================================
DROP PROCEDURE IF EXISTS InsertJobs$$
CREATE PROCEDURE InsertJobs()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE job_titles VARCHAR(50);
    DECLARE categories VARCHAR(50);
    DECLARE cities VARCHAR(20);
    DECLARE educations VARCHAR(20);
    
    WHILE i <= 300 DO
        SET job_titles = ELT(i % 15 + 1, 
            'Java开发工程师', '前端开发工程师', '产品经理', 'UI设计师', '测试工程师',
            '运维工程师', '数据分析师', '算法工程师', '产品经理', '项目经理',
            'Android开发', 'iOS开发', 'Go开发工程师', 'Python开发', '全栈工程师');
        SET categories = ELT(i % 20 + 1, 
            '计算机软件', '互联网', '电子商务', '人工智能', '大数据',
            '计算机服务', '企业服务', '社交网络与媒体', '游戏', '云计算',
            '半导体/芯片', '电子/硬件开发', '通信/网络设备', '银行', '证券',
            '保险', '房地产开发', '建筑设计', '汽车研发', '汽车制造');
        SET cities = ELT(i % 6 + 1, '北京', '上海', '深圳', '杭州', '广州', '成都');
        SET educations = ELT(i % 4 + 1, '大专', '本科', '本科', '硕士');
        
        -- 随机选择一个企业 (ID范围: 3-52)
        SET @company_id = 3 + FLOOR(RAND() * 50);
        
        INSERT INTO job_info (company_id, title, category, salary_min, salary_max, salary_month,
            work_city, work_address, experience, education, job_type, job_desc, requirements,
            welfare, status, view_count, apply_count, publish_time, deadline)
        VALUES (
            @company_id,
            job_titles,
            categories,
            8000 + (i % 30) * 1000,
            15000 + (i % 50) * 1000,
            12 + i % 4,
            cities,
            CONCAT(cities, '市高新区科技园'),
            ELT(i % 5 + 1, '1-3年', '3-5年', '1-3年', '3-5年', '5-10年'),
            educations,
            1,
            CONCAT('负责公司', categories, '相关工作，参与', job_titles, '的开发和维护。'),
            CONCAT('1. ', ELT(i % 3 + 1, '1-3年', '2-4年', '3-5年'), '相关工作经验\n2. 熟悉', categories, '相关技术\n3. 具备良好的沟通能力和团队协作精神\n4. 有大型项目经验优先'),
            '五险一金、年终奖、带薪年假、节日福利、定期体检、团建活动',
            1,
            FLOOR(RAND() * 2000),
            FLOOR(RAND() * 100),
            DATE_SUB(CURDATE(), INTERVAL i % 30 DAY),
            DATE_ADD(CURDATE(), INTERVAL (30 + i % 60) DAY)
        );
        
        SET i = i + 1;
    END WHILE;
END$$

-- ============================================
-- 4. 批量生成投递记录 (500条)
-- ============================================
DROP PROCEDURE IF EXISTS InsertApplications$$
CREATE PROCEDURE InsertApplications()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE job_id_val INT;
    DECLARE company_id_val INT;
    DECLARE user_id_val INT;
    DECLARE resume_id_val INT;
    
    WHILE i <= 500 DO
        -- 随机选择职位 (ID范围: 1-300)
        SET job_id_val = 1 + FLOOR(RAND() * 300);
        -- 获取职位对应的公司ID
        SELECT company_id INTO company_id_val FROM job_info WHERE id = job_id_val LIMIT 1;
        
        -- 随机选择求职者 (ID范围: 53-252, 对应200名求职者)
        SET user_id_val = 53 + FLOOR(RAND() * 200);
        -- 获取对应的简历ID
        SELECT id INTO resume_id_val FROM resume WHERE user_id = user_id_val LIMIT 1;
        
        -- 如果没有找到简历，跳过
        IF resume_id_val IS NOT NULL THEN
            INSERT IGNORE INTO application (job_id, resume_id, user_id, company_id, status, remark, apply_time)
            VALUES (
                job_id_val,
                resume_id_val,
                user_id_val,
                company_id_val,
                FLOOR(RAND() * 4),
                ELT(FLOOR(RAND() * 4) + 1, NULL, '技术能力符合要求', '经验不太匹配', '等待面试'),
                DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 30) DAY)
            );
        END IF;
        
        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

-- ============================================
-- 执行存储过程
-- ============================================
CALL InsertCompanies();
CALL InsertJobSeekers();
CALL InsertJobs();
CALL InsertApplications();

-- 删除存储过程
DROP PROCEDURE IF EXISTS InsertCompanies;
DROP PROCEDURE IF EXISTS InsertJobSeekers;
DROP PROCEDURE IF EXISTS InsertJobs;
DROP PROCEDURE IF EXISTS InsertApplications;

-- 查看统计结果
SELECT '用户表' as table_name, COUNT(*) as count FROM sys_user
UNION ALL
SELECT '企业信息表', COUNT(*) FROM company_info
UNION ALL
SELECT '招聘信息表', COUNT(*) FROM job_info
UNION ALL
SELECT '简历表', COUNT(*) FROM resume
UNION ALL
SELECT '投递记录表', COUNT(*) FROM application;
