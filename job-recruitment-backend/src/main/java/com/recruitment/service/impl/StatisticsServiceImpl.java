package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.dto.Result;
import com.recruitment.entity.*;
import com.recruitment.mapper.*;
import com.recruitment.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.recruitment.utils.JobCategoryResolver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CompanyMapper companyMapper;
    
    @Autowired
    private JobMapper jobMapper;
    
    @Autowired
    private ApplicationMapper applicationMapper;

    // 瀹氫箟琛屼笟澶х被涓庡瓙绫荤殑鏄犲皠鍏崇郴
    @Override
    public Result<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 1. 鍩虹缁熻鏁版嵁
        statistics.put("totalUsers", countUsers());
        statistics.put("totalCompanies", countCompanies());
        statistics.put("totalJobs", countJobs());
        statistics.put("totalApplications", countApplications());
        
        // 2. 鐢ㄦ埛瑙掕壊鍒嗗竷
        statistics.put("roleDistribution", getRoleDistribution());
        
        // 3. 杩?澶╃敤鎴锋敞鍐岃秼鍔?
        statistics.put("userTrend", getUserTrend());
        
        // 4. 鑱屼綅绫诲埆鍒嗗竷锛堟寜澶х被缁熻锛?
        statistics.put("jobCategoryDistribution", getJobCategoryDistribution());
        
        // 5. 杩?澶╄亴浣嶅彂甯冭秼鍔?
        statistics.put("jobTrend", getJobTrend());
        
        // 6. 鎶曢€掔姸鎬佸垎甯?
        statistics.put("applicationStatusDistribution", getApplicationStatusDistributionV2());
        
        // 7. 鐑棬鍩庡競TOP10
        statistics.put("topCities", getTopCities());
        
        return Result.success(statistics);
    }
    
    private Long countUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        return userMapper.selectCount(wrapper);
    }
    
    private Long countCompanies() {
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Company::getDeleted, 0);
        return companyMapper.selectCount(wrapper);
    }
    
    private Long countJobs() {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getDeleted, 0);
        return jobMapper.selectCount(wrapper);
    }
    
    private Long countApplications() {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getDeleted, 0);
        return applicationMapper.selectCount(wrapper);
    }
    
    private List<Map<String, Object>> getRoleDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 鏌ヨ鍚勮鑹叉暟閲?
        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getRole, 1).eq(User::getDeleted, 0);
        long adminCount = userMapper.selectCount(adminWrapper);
        
        LambdaQueryWrapper<User> operatorWrapper = new LambdaQueryWrapper<>();
        operatorWrapper.eq(User::getRole, 2).eq(User::getDeleted, 0);
        long operatorCount = userMapper.selectCount(operatorWrapper);
        
        LambdaQueryWrapper<User> companyWrapper = new LambdaQueryWrapper<>();
        companyWrapper.eq(User::getRole, 3).eq(User::getDeleted, 0);
        long companyCount = userMapper.selectCount(companyWrapper);
        
        LambdaQueryWrapper<User> jobseekerWrapper = new LambdaQueryWrapper<>();
        jobseekerWrapper.eq(User::getRole, 4).eq(User::getDeleted, 0);
        long jobseekerCount = userMapper.selectCount(jobseekerWrapper);
        
        result.add(createChartData("管理员", adminCount, "#f56c6c"));
        result.add(createChartData("运营", operatorCount, "#e6a23c"));
        result.add(createChartData("企业", companyCount, "#67c23a"));
        result.add(createChartData("求职者", jobseekerCount, "#409eff"));
        
        return result;
    }
    
    private Map<String, Object> getUserTrend() {
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            dates.add(date.format(formatter));
            
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getDeleted, 0)
                   .ge(User::getCreateTime, date.truncatedTo(ChronoUnit.DAYS))
                   .lt(User::getCreateTime, date.plusDays(1).truncatedTo(ChronoUnit.DAYS));
            counts.add(userMapper.selectCount(wrapper));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }
    private List<Map<String, Object>> getJobCategoryDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        // query category data
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getDeleted, 0).select(Job::getCategory);
        List<Job> jobs = jobMapper.selectList(wrapper);

        // count raw category values
        Map<String, Long> categoryCount = new HashMap<>();
        for (Job job : jobs) {
            String category = job.getCategory();
            if (category != null && !category.isBlank()) {
                categoryCount.put(category, categoryCount.getOrDefault(category, 0L) + 1);
            }
        }

        // aggregate by major categories in unified resolver
        Map<String, Long> categoryGroupCount = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : JobCategoryResolver.getCategoryMap().entrySet()) {
            String majorCategory = entry.getKey();
            List<String> subCategories = entry.getValue();
            long count = 0;
            for (String subCategory : subCategories) {
                count += categoryCount.getOrDefault(subCategory, 0L);
            }
            if (count > 0) {
                categoryGroupCount.put(majorCategory, count);
            }
        }

        long totalCategoryCount = categoryCount.values().stream().mapToLong(Long::longValue).sum();
        long groupedCount = categoryGroupCount.values().stream().mapToLong(Long::longValue).sum();
        long otherCount = totalCategoryCount - groupedCount;
        if (otherCount > 0) {
            categoryGroupCount.put("Other", otherCount);
        }

        for (Map.Entry<String, Long> entry : categoryGroupCount.entrySet()) {
            result.add(createChartData(entry.getKey(), entry.getValue(), null));
        }

        return result;
    }
    private Map<String, Object> getJobTrend() {
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            dates.add(date.format(formatter));
            
            LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Job::getDeleted, 0)
                   .ge(Job::getCreateTime, date.truncatedTo(ChronoUnit.DAYS))
                   .lt(Job::getCreateTime, date.plusDays(1).truncatedTo(ChronoUnit.DAYS));
            counts.add(jobMapper.selectCount(wrapper));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        return result;
    }
    
    private List<Map<String, Object>> getApplicationStatusDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 寰呭鐞?
        LambdaQueryWrapper<Application> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Application::getStatus, 0).eq(Application::getDeleted, 0);
        long pendingCount = applicationMapper.selectCount(pendingWrapper);
        
        // 宸查€氳繃
        LambdaQueryWrapper<Application> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Application::getStatus, 1).eq(Application::getDeleted, 0);
        long approvedCount = applicationMapper.selectCount(approvedWrapper);
        
        // 宸叉嫆缁?
        LambdaQueryWrapper<Application> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(Application::getStatus, 2).eq(Application::getDeleted, 0);
        long rejectedCount = applicationMapper.selectCount(rejectedWrapper);
        
        result.add(createChartData("待处理", pendingCount, "#e6a23c"));
        result.add(createChartData("已通过", approvedCount, "#67c23a"));
        result.add(createChartData("已拒绝", rejectedCount, "#f56c6c"));
        
        return result;
    }
    
    private List<Map<String, Object>> getApplicationStatusDistributionV2() {
        List<Map<String, Object>> result = new ArrayList<>();

        long pendingCount = countApplicationsByStatus(Application.STATUS_PENDING);
        long viewedCount = countApplicationsByStatus(Application.STATUS_VIEWED);
        long interestedCount = countApplicationsByStatus(Application.STATUS_INTERESTED);
        long unsuitableCount = countApplicationsByStatus(Application.STATUS_UNSUITABLE);
        long interviewingCount = countApplicationsByStatus(Application.STATUS_INTERVIEWING);
        long interviewFailCount = countApplicationsByStatus(Application.STATUS_INTERVIEW_FAILED);
        long interviewPassedCount = countApplicationsByStatus(Application.STATUS_INTERVIEW_PASSED);
        long offerPendingCount = countApplicationsByStatus(Application.STATUS_OFFER_PENDING);
        long offerAcceptedCount = countApplicationsByStatus(Application.STATUS_OFFER_ACCEPTED);
        long offerRejectedCount = countApplicationsByStatus(Application.STATUS_OFFER_REJECTED);
        long offerExpiredCount = countApplicationsByStatus(Application.STATUS_OFFER_EXPIRED);
        long onboardedCount = countApplicationsByStatus(Application.STATUS_ONBOARDED);

        if (pendingCount > 0) {
            result.add(createChartData("待查看", pendingCount, "#e6a23c"));
        }
        if (viewedCount > 0) {
            result.add(createChartData("已查看", viewedCount, "#409eff"));
        }
        if (interestedCount > 0) {
            result.add(createChartData("感兴趣", interestedCount, "#67c23a"));
        }
        if (unsuitableCount > 0) {
            result.add(createChartData("不合适", unsuitableCount, "#909399"));
        }
        if (interviewingCount > 0) {
            result.add(createChartData("面试中", interviewingCount, "#b37feb"));
        }
        if (interviewFailCount > 0) {
            result.add(createChartData("面试未通过", interviewFailCount, "#f56c6c"));
        }
        if (interviewPassedCount > 0) {
            result.add(createChartData("面试通过", interviewPassedCount, "#67c23a"));
        }
        if (offerPendingCount > 0) {
            result.add(createChartData("Offer待确认", offerPendingCount, "#e6a23c"));
        }
        if (offerAcceptedCount > 0) {
            result.add(createChartData("Offer已接受", offerAcceptedCount, "#67c23a"));
        }
        if (offerRejectedCount > 0) {
            result.add(createChartData("Offer已拒绝", offerRejectedCount, "#ffb400"));
        }
        if (offerExpiredCount > 0) {
            result.add(createChartData("Offer已过期", offerExpiredCount, "#909399"));
        }
        if (onboardedCount > 0) {
            result.add(createChartData("已入职", onboardedCount, "#34bfa3"));
        }
        return result;
    }

    private long countApplicationsByStatus(int status) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getStatus, status).eq(Application::getDeleted, 0);
        return applicationMapper.selectCount(wrapper);
    }

    private Map<String, Object> getTopCities() {
        // 鏌ヨ鎵€鏈夎亴浣嶇殑鍩庡競
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getDeleted, 0).select(Job::getWorkCity);
        List<Job> jobs = jobMapper.selectList(wrapper);
        
        // 缁熻姣忎釜鍩庡競鐨勬暟閲?
        Map<String, Long> cityCount = new HashMap<>();
        for (Job job : jobs) {
            String city = job.getWorkCity();
            if (city != null && !city.isEmpty()) {
                cityCount.put(city, cityCount.getOrDefault(city, 0L) + 1);
            }
        }
        
        // 鎺掑簭骞跺彇鍓?0
        List<Map.Entry<String, Long>> sortedList = new ArrayList<>(cityCount.entrySet());
        sortedList.sort(Map.Entry.<String, Long>comparingByValue().reversed());
        
        List<String> cities = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        int limit = Math.min(10, sortedList.size());
        for (int i = 0; i < limit; i++) {
            cities.add(sortedList.get(i).getKey());
            counts.add(sortedList.get(i).getValue());
        }
        
        // 鍙嶈浆鍒楄〃锛岃鏈€澶氱殑鍦ㄥ簳閮?
        Collections.reverse(cities);
        Collections.reverse(counts);
        
        Map<String, Object> result = new HashMap<>();
        result.put("cities", cities);
        result.put("counts", counts);
        return result;
    }
    
    private Map<String, Object> createChartData(String name, Long value, String color) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("value", value);
        if (color != null) {
            data.put("itemStyle", Collections.singletonMap("color", color));
        }
        return data;
    }
}



