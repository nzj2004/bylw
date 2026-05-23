package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recruitment.dto.*;
import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.mapper.CompanyMapper;
import com.recruitment.mapper.JobMapper;
import com.recruitment.utils.JobCategoryResolver;
import com.recruitment.service.JobService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public Result<Boolean> createJob(Job job, Long companyId) {
        Company company = companyMapper.selectById(companyId);
        if (company == null) {
            return Result.error("企业不存在");
        }
        job.setCompanyId(companyId);
        job.setStatus(Job.STATUS_PENDING);
        job.setViewCount(0);
        job.setApplyCount(0);
        baseMapper.insert(job);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> updateJob(Job job) {
        Job existing = baseMapper.selectById(job.getId());
        if (existing == null) {
            return Result.error("职位不存在");
        }
        job.setCompanyId(existing.getCompanyId());
        job.setStatus(Job.STATUS_PENDING);
        baseMapper.updateById(job);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> deleteJob(Long id) {
        baseMapper.deleteById(id);
        return Result.success(true);
    }

    @Override
    public Result<JobDTO> getJobById(Long id) {
        Job job = baseMapper.selectById(id);
        if (job == null) {
            return Result.error("职位不存在");
        }
        return Result.success(convertToDTO(job));
    }

    public Result<PageResult<JobDTO>> listJobs(String keyword, String category, String city,
                                                String experience, String education,
                                                Integer minSalary, Integer maxSalary,
                                                Integer status, Long current, Long size) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();

        // 濡傛灉鏈夊叧閿瓧锛屼娇鐢ㄨ仈琛ㄦ煡璇㈡悳绱㈣亴浣嶆爣棰樸€佹弿杩板拰鍏徃鍚嶇О
        if (StringUtils.hasText(keyword)) {
            List<Long> jobIds = baseMapper.selectJobIdsByKeyword(keyword);
            if (jobIds == null || jobIds.isEmpty()) {
                // 娌℃湁鍖归厤缁撴灉锛岃繑鍥炵┖
                return Result.success(PageResult.of(0L, current, size, List.of()));
            }
            wrapper.in(Job::getId, jobIds);
        }
        if (StringUtils.hasText(category)) {
            // 妫€鏌ユ槸鍚︽槸澶х被锛屽鏋滄槸鍒欐煡璇㈡墍鏈夊瓙绫?
            List<String> categoryValues = JobCategoryResolver.resolveCategoryValues(category);
            if (!categoryValues.isEmpty()) {
                wrapper.in(Job::getCategory, categoryValues);
            }
        }
        if (StringUtils.hasText(city)) {
            wrapper.like(Job::getWorkCity, city);
        }
        if (StringUtils.hasText(experience)) {
            wrapper.like(Job::getExperience, experience);
        }
        if (StringUtils.hasText(education)) {
            wrapper.like(Job::getEducation, education);
        }
        if (minSalary != null || maxSalary != null) {
            Integer normalizedMinSalary = minSalary;
            Integer normalizedMaxSalary = maxSalary;

            if (normalizedMinSalary != null && normalizedMaxSalary != null && normalizedMinSalary > normalizedMaxSalary) {
                Integer swap = normalizedMinSalary;
                normalizedMinSalary = normalizedMaxSalary;
                normalizedMaxSalary = swap;
            }

            if (normalizedMinSalary != null && normalizedMaxSalary != null) {
                wrapper.le(Job::getSalaryMin, normalizedMaxSalary);
                wrapper.ge(Job::getSalaryMax, normalizedMinSalary);
            } else if (normalizedMinSalary != null) {
                wrapper.ge(Job::getSalaryMax, normalizedMinSalary);
            } else if (normalizedMaxSalary != null) {
                wrapper.le(Job::getSalaryMin, normalizedMaxSalary);
            }
        }
        if (status != null) {
            wrapper.eq(Job::getStatus, status);
        } else {
            wrapper.eq(Job::getStatus, Job.STATUS_PUBLISHED);
        }

        wrapper.orderByDesc(Job::getPublishTime, Job::getCreateTime);

        Page<Job> page = new Page<>(current, size);
        baseMapper.selectPage(page, wrapper);

        List<JobDTO> records = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(page.getTotal(), current, size, records));
    }

    @Override
    public Result<PageResult<JobDTO>> listHotJobs(Long current, Long size) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getStatus, Job.STATUS_PUBLISHED);
        wrapper.orderByDesc(Job::getApplyCount, Job::getViewCount, Job::getPublishTime, Job::getCreateTime);

        Page<Job> page = new Page<>(current, size);
        baseMapper.selectPage(page, wrapper);

        List<JobDTO> records = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(page.getTotal(), current, size, records));
    }

    @Override
    public Result<PageResult<JobDTO>> listCompanyJobs(Long companyId, String keyword, String category, Integer status, Long current, Long size) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getCompanyId, companyId);
        
        // 鍏抽敭瀛楁悳绱?
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Job::getTitle, keyword);
        }
        
        // 鍒嗙被绛涢€夛紙鏀寔澶х被鍜屽瓙绫伙級
        if (StringUtils.hasText(category)) {
            List<String> categoryValues = JobCategoryResolver.resolveCategoryValues(category);
            if (!categoryValues.isEmpty()) {
                wrapper.in(Job::getCategory, categoryValues);
            }
        }
        
        if (status != null) {
            wrapper.eq(Job::getStatus, status);
        }
        wrapper.orderByDesc(Job::getCreateTime);
        
        Page<Job> page = new Page<>(current, size);
        baseMapper.selectPage(page, wrapper);
        
        List<JobDTO> records = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return Result.success(PageResult.of(page.getTotal(), current, size, records));
    }

    @Override
    public Result<Boolean> auditJob(Long id, Integer status, String reason) {
        Job job = new Job();
        job.setId(id);
        job.setStatus(status);
        if (status == Job.STATUS_REJECTED) {
            job.setRejectReason(reason);
        }
        if (status == Job.STATUS_PUBLISHED) {
            job.setPublishTime(LocalDateTime.now());
        }
        baseMapper.updateById(job);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> batchAuditJobs(List<Long> jobIds, Integer status, String reason) {
        if (jobIds == null || jobIds.isEmpty()) {
            return Result.error("璇烽€夋嫨瑕佸鏍哥殑鑱屼綅");
        }
        
        // 鎵归噺鏇存柊鑱屼綅鐘舵€?
        for (Long jobId : jobIds) {
            Job job = new Job();
            job.setId(jobId);
            job.setStatus(status);
            if (status == Job.STATUS_REJECTED) {
                job.setRejectReason(reason);
            }
            if (status == Job.STATUS_PUBLISHED) {
                job.setPublishTime(LocalDateTime.now());
            }
            baseMapper.updateById(job);
        }
        
        return Result.success(true);
    }

    @Override
    public Result<Boolean> batchPublishJobs(List<Job> jobs, Long adminId) {
        for (Job job : jobs) {
            Company company = companyMapper.selectById(job.getCompanyId());
            if (company == null) {
                return Result.error("企业不存在，ID：" + job.getCompanyId());
            }
            job.setStatus(Job.STATUS_PUBLISHED);
            job.setPublishTime(LocalDateTime.now());
            job.setViewCount(0);
            job.setApplyCount(0);
            baseMapper.insert(job);
        }
        return Result.success(true);
    }

    @Override
    public Result<Boolean> incrementViewCount(Long id) {
        baseMapper.incrementViewCount(id);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> incrementApplyCount(Long id) {
        baseMapper.incrementApplyCount(id);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> toggleJobStatus(Long id, Integer status) {
        Job job = new Job();
        job.setId(id);
        job.setStatus(status);
        baseMapper.updateById(job);
        return Result.success(true);
    }

    private JobDTO convertToDTO(Job job) {
        JobDTO dto = new JobDTO();
        BeanUtils.copyProperties(job, dto);
        dto.setSalaryRange(job.getSalaryRange());
        dto.setStatusName(job.getStatusName());
        dto.setJobTypeName(job.getJobTypeName());
        
        Company company = companyMapper.selectById(job.getCompanyId());
        if (company != null) {
            dto.setCompanyName(company.getCompanyName());
            dto.setCompanyLogo(company.getLogoUrl());
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (job.getPublishTime() != null) {
            dto.setPublishTime(job.getPublishTime().format(formatter));
        }
        if (job.getDeadline() != null) {
            dto.setDeadline(job.getDeadline().toString());
        }
        dto.setCreateTime(job.getCreateTime().format(formatter));
        
        return dto;
    }
}

