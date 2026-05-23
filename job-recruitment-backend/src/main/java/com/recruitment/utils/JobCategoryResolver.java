package com.recruitment.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JobCategoryResolver {

    private static final Map<String, List<String>> CATEGORY_MAP = new LinkedHashMap<>();
    private static final Map<String, String> SUBCATEGORY_CANONICAL = new HashMap<>();

    static {
        putMajor("互联网/AI", Arrays.asList(
            "互联网",
            "电子商务",
            "计算机软件",
            "生活服务(O2O)",
            "企业服务",
            "医疗健康",
            "游戏",
            "社交网络与媒体",
            "人工智能",
            "云计算",
            "在线教育",
            "计算机服务",
            "新零售",
            "大数据",
            "广告营销",
            "物联网",
            "信息安全"
        ));
        putMajor("电子/通信/半导体", Arrays.asList(
            "半导体/芯片",
            "电子/硬件开发",
            "通信/网络设备",
            "智能硬件/消费电子",
            "运营商/增值服务",
            "计算机硬件"
        ));
        putMajor("金融", Arrays.asList(
            "银行",
            "证券",
            "保险",
            "基金",
            "信托",
            "互联网金融",
            "投资"
        ));
        putMajor("房地产/建筑", Arrays.asList(
            "房地产开发",
            "物业管理",
            "建筑设计",
            "工程施工",
            "装饰装修",
            "建材"
        ));
        putMajor("汽车/机械/制造", Arrays.asList(
            "汽车研发",
            "汽车制造",
            "汽车零部件",
            "机械设计",
            "机械制造",
            "自动化设备"
        ));
        putMajor("消费品/零售", Arrays.asList(
            "快消品",
            "零售",
            "服装纺织",
            "食品饮料",
            "化妆品",
            "家居用品"
        ));
        putMajor("教育/培训", Arrays.asList(
            "学前教育",
            "K12教育",
            "高等教育",
            "职业培训",
            "语言培训",
            "素质教育"
        ));
        putMajor("物流/交通/贸易", Arrays.asList(
            "物流",
            "快递",
            "仓储",
            "航运",
            "航空",
            "国际贸易",
            "进出口"
        ));
        putMajor("能源/化工/环保", Arrays.asList(
            "石油石化",
            "新能源",
            "电力",
            "化工",
            "环保",
            "新材料"
        ));
        putMajor("专业服务", Arrays.asList(
            "会计审计",
            "法律咨询",
            "人力资源",
            "管理咨询",
            "广告公关",
            "检测认证"
        ));
        putMajor("政府/非营利组织", Arrays.asList(
            "政府机关",
            "事业单位",
            "科研机构",
            "社会组织",
            "公益慈善"
        ));

        // 同义词（数据库历史数据修复时常见）
        putAlias("后端开发", "计算机软件");
        putAlias("前端开发", "计算机软件");
        putAlias("移动端开发", "计算机软件");
        putAlias("测试", "计算机软件");
        putAlias("运维", "计算机软件");
        putAlias("测试工程师", "计算机软件");
        putAlias("后端开发工程师", "计算机软件");
        putAlias("前端开发工程师", "计算机软件");
        putAlias("产品", "计算机软件");
        putAlias("产品经理", "计算机软件");
        putAlias("技术经理", "企业服务");
        putAlias("项目经理", "企业服务");
        putAlias("项目管理", "企业服务");
        putAlias("UI设计", "计算机服务");
        putAlias("UI设计师", "计算机服务");
        putAlias("UI", "计算机服务");
        putAlias("算法/AI", "人工智能");
        putAlias("算法工程师", "人工智能");
        putAlias("大数据分析", "大数据");
        putAlias("数据分析师", "大数据");
        putAlias("在线教育", "在线教育");
        putAlias("社交", "社交网络与媒体");
    }

    private static void putMajor(String major, List<String> subCategories) {
        List<String> frozen = Collections.unmodifiableList(new ArrayList<>(subCategories));
        CATEGORY_MAP.put(major, frozen);
        for (String sub : frozen) {
            SUBCATEGORY_CANONICAL.put(sub, sub);
        }
    }

    private static void putAlias(String alias, String canonicalSubCategory) {
        SUBCATEGORY_CANONICAL.put(alias, canonicalSubCategory);
    }

    public static Map<String, List<String>> getCategoryMap() {
        return CATEGORY_MAP;
    }

    public static List<String> resolveCategoryValues(String category) {
        if (!hasText(category)) {
            return Collections.emptyList();
        }

        String normalized = category.trim();

        if (CATEGORY_MAP.containsKey(normalized)) {
            List<String> subCategories = CATEGORY_MAP.get(normalized);
            List<String> merged = new ArrayList<>(subCategories);
            merged.add(normalized);
            return merged;
        }

        String canonical = SUBCATEGORY_CANONICAL.get(normalized);
        if (canonical != null) {
            return Collections.singletonList(canonical);
        }
        return Collections.singletonList(normalized);
    }

    public static List<String> resolveCategoryValues(String majorCategory, String subCategory) {
        if (hasText(subCategory)) {
            return resolveCategoryValues(subCategory);
        }
        if (hasText(majorCategory)) {
            return resolveCategoryValues(majorCategory);
        }
        return Collections.emptyList();
    }

    public static Set<String> getAllNormalizedValues() {
        return CATEGORY_MAP.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private JobCategoryResolver() {
    }
}
