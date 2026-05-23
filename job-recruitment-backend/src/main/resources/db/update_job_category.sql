-- 统一职位分类数据到大类/小类标准口径（Navicat 可直接导入）
-- 目标：
-- 1. 统一历史数据中的职位分类；
-- 2. 后端/前端可使用同一套口径做“大类”和“小类”筛选；
-- 3. 将未识别分类先归入“其他”，便于人工核对。

USE job_recruitment;

-- 0) 清理空格，便于稳定匹配
UPDATE job_info
SET category = NULLIF(TRIM(category), '')
WHERE deleted = 0;

-- 1) 创建标准化映射表（一次性导入）
DROP TEMPORARY TABLE IF EXISTS tmp_job_category_map;
CREATE TEMPORARY TABLE tmp_job_category_map (
    old_category VARCHAR(80) NOT NULL PRIMARY KEY,
    normalized_category VARCHAR(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_job_category_map (old_category, normalized_category) VALUES
  -- 大类（分类筛选第一层）
  ('互联网/AI', '互联网/AI'),
  ('电子/通信/半导体', '电子/通信/半导体'),
  ('金融', '金融'),
  ('房地产/建筑', '房地产/建筑'),
  ('汽车/机械/制造', '汽车/机械/制造'),
  ('消费品/零售', '消费品/零售'),
  ('教育/培训', '教育/培训'),
  ('物流/交通/贸易', '物流/交通/贸易'),
  ('能源/化工/环保', '能源/化工/环保'),
  ('专业服务', '专业服务'),
  ('政府/非营利组织', '政府/非营利组织'),

  -- 子类（分类筛选第二层）
  ('互联网', '互联网'),
  ('电子商务', '电子商务'),
  ('计算机软件', '计算机软件'),
  ('生活服务(O2O)', '生活服务(O2O)'),
  ('企业服务', '企业服务'),
  ('医疗健康', '医疗健康'),
  ('游戏', '游戏'),
  ('社交网络与媒体', '社交网络与媒体'),
  ('人工智能', '人工智能'),
  ('云计算', '云计算'),
  ('在线教育', '在线教育'),
  ('计算机服务', '计算机服务'),
  ('大数据', '大数据'),
  ('广告营销', '广告营销'),
  ('物联网', '物联网'),
  ('新零售', '新零售'),
  ('信息安全', '信息安全'),
  ('半导体/芯片', '半导体/芯片'),
  ('电子/硬件开发', '电子/硬件开发'),
  ('通信/网络设备', '通信/网络设备'),
  ('智能硬件/消费电子', '智能硬件/消费电子'),
  ('运营商/增值服务', '运营商/增值服务'),
  ('计算机硬件', '计算机硬件'),
  ('银行', '银行'),
  ('证券', '证券'),
  ('保险', '保险'),
  ('基金', '基金'),
  ('信托', '信托'),
  ('互联网金融', '互联网金融'),
  ('投资', '投资'),
  ('房地产开发', '房地产开发'),
  ('物业管理', '物业管理'),
  ('建筑设计', '建筑设计'),
  ('工程施工', '工程施工'),
  ('装饰装修', '装饰装修'),
  ('建材', '建材'),
  ('汽车研发', '汽车研发'),
  ('汽车制造', '汽车制造'),
  ('汽车零部件', '汽车零部件'),
  ('机械设计', '机械设计'),
  ('机械制造', '机械制造'),
  ('自动化设备', '自动化设备'),
  ('快消品', '快消品'),
  ('零售', '零售'),
  ('服装纺织', '服装纺织'),
  ('食品饮料', '食品饮料'),
  ('化妆品', '化妆品'),
  ('家居用品', '家居用品'),
  ('学前教育', '学前教育'),
  ('K12教育', 'K12教育'),
  ('高等教育', '高等教育'),
  ('职业培训', '职业培训'),
  ('语言培训', '语言培训'),
  ('素质教育', '素质教育'),
  ('物流', '物流'),
  ('快递', '快递'),
  ('仓储', '仓储'),
  ('航运', '航运'),
  ('航空', '航空'),
  ('国际贸易', '国际贸易'),
  ('进出口', '进出口'),
  ('石油石化', '石油石化'),
  ('新能源', '新能源'),
  ('电力', '电力'),
  ('化工', '化工'),
  ('环保', '环保'),
  ('新材料', '新材料'),
  ('会计审计', '会计审计'),
  ('法律咨询', '法律咨询'),
  ('人力资源', '人力资源'),
  ('管理咨询', '管理咨询'),
  ('广告公关', '广告公关'),
  ('检测认证', '检测认证'),
  ('政府机关', '政府机关'),
  ('事业单位', '事业单位'),
  ('科研机构', '科研机构'),
  ('社会组织', '社会组织'),
  ('公益慈善', '公益慈善'),

  -- 历史别名（常见历史脏数据，映射到标准口径）
  ('IT', '计算机软件'),
  ('软件', '计算机软件'),
  ('互联网行业', '互联网'),
  ('后端开发', '计算机软件'),
  ('后端开发工程师', '计算机软件'),
  ('前端开发', '计算机软件'),
  ('前端开发工程师', '计算机软件'),
  ('移动端开发', '计算机软件'),
  ('测试', '计算机软件'),
  ('测试工程师', '计算机软件'),
  ('运维', '计算机软件'),
  ('运维工程师', '计算机软件'),
  ('产品', '计算机软件'),
  ('产品经理', '计算机软件'),
  ('需求', '计算机软件'),
  ('产品运营', '计算机软件'),
  ('技术经理', '企业服务'),
  ('项目经理', '企业服务'),
  ('项目管理', '企业服务'),
  ('项目负责人', '企业服务'),
  ('PM', '企业服务'),
  ('UI设计', '计算机服务'),
  ('UI设计师', '计算机服务'),
  ('UI', '计算机服务'),
  ('算法', '人工智能'),
  ('算法/AI', '人工智能'),
  ('算法工程师', '人工智能'),
  ('大数据分析', '大数据'),
  ('数据分析师', '大数据'),
  ('销售经理', '专业服务'),
  ('商务', '专业服务'),
  ('采购', '专业服务'),
  ('社交', '社交网络与媒体'),
  ('在线教育', '在线教育'),
  ('其他', '其他');

-- 2) 用映射关系把历史数据统一到标准分类
UPDATE job_info j
INNER JOIN tmp_job_category_map m
  ON j.category = m.old_category
SET j.category = m.normalized_category
WHERE j.deleted = 0;

-- 3) 先看“未匹配到标准口径”的剩余值（用于人工核对）
SELECT 'unmapped_before_fallback' AS stage, j.category AS category_value, COUNT(*) AS count
FROM job_info j
LEFT JOIN (
  SELECT DISTINCT normalized_category
  FROM tmp_job_category_map
) map_tbl
  ON j.category = map_tbl.normalized_category
WHERE j.deleted = 0
  AND j.category IS NOT NULL
  AND map_tbl.normalized_category IS NULL
GROUP BY j.category
ORDER BY count DESC;

-- 4) 未匹配的归入“其他”
UPDATE job_info j
LEFT JOIN (
  SELECT DISTINCT normalized_category
  FROM tmp_job_category_map
) map_tbl
  ON j.category = map_tbl.normalized_category
SET j.category = '其他'
WHERE j.deleted = 0
  AND j.category IS NOT NULL
  AND map_tbl.normalized_category IS NULL;

-- 5) 标准化后结果
SELECT 'after_normalize' AS stage, category, COUNT(*) AS count
FROM job_info
WHERE deleted = 0
GROUP BY category
ORDER BY count DESC;

DROP TEMPORARY TABLE IF EXISTS tmp_job_category_map;
