package com.recruitment.controller;

import com.recruitment.dto.*;
import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.mapper.CompanyMapper;
import com.recruitment.service.JobService;
import com.recruitment.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private CompanyMapper companyMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public Result<Boolean> createJob(@RequestBody Job job) {
        Long userId = SecurityUtil.getCurrentUserId();
        Integer role = SecurityUtil.getCurrentRole();
        
        Long companyId;
        if (role == 1) {
            companyId = job.getCompanyId();
            Company company = companyMapper.selectById(companyId);
            if (company == null) {
                return Result.error("企业不存在");
            }
        } else {
            Company company = companyMapper.selectByUserId(userId);
            if (company == null) {
                return Result.error("请先填写企业信息");
            }
            if (company.getStatus() == null || company.getStatus() != Company.STATUS_APPROVED) {
                return Result.error("企业审核通过后才能发布职位");
            }
            companyId = company.getId();
        }
        
        return jobService.createJob(job, companyId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public Result<Boolean> updateJob(@PathVariable Long id, @RequestBody Job job) {
        if (!canManageJob(id)) {
            return Result.error("无权操作该职位");
        }
        job.setId(id);
        return jobService.updateJob(job);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public Result<Boolean> deleteJob(@PathVariable Long id) {
        if (!canManageJob(id)) {
            return Result.error("无权操作该职位");
        }
        return jobService.deleteJob(id);
    }

    @GetMapping("/{id}")
    public Result<JobDTO> getJobById(@PathVariable Long id) {
        jobService.incrementViewCount(id);
        return jobService.getJobById(id);
    }

    @GetMapping("/list")
    public Result<PageResult<JobDTO>> listJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxSalary,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return jobService.listJobs(keyword, category, city, experience, education, minSalary, maxSalary, status, current, size);
    }

    @GetMapping("/hot")
    public Result<PageResult<JobDTO>> listHotJobs(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "6") Long size) {
        return jobService.listHotJobs(current, size);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public Result<PageResult<JobDTO>> listCompanyJobs(
            @PathVariable Long companyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long resolvedCompanyId = companyId;
        Integer role = SecurityUtil.getCurrentRole();
        if (role != null && role == 3) {
            Company company = companyMapper.selectByUserId(SecurityUtil.getCurrentUserId());
            if (company == null) {
                return Result.error("请先填写企业信息");
            }
            resolvedCompanyId = company.getId();
        }
        return jobService.listCompanyJobs(resolvedCompanyId, keyword, category, status, current, size);
    }

    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public Result<Boolean> auditJob(@PathVariable Long id, 
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String reason) {
        return jobService.auditJob(id, status, reason);
    }

    @PutMapping("/batch-audit")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public Result<Boolean> batchAuditJobs(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> jobIds = (List<Long>) params.get("jobIds");
        Integer status = (Integer) params.get("status");
        String reason = (String) params.get("reason");
        return jobService.batchAuditJobs(jobIds, status, reason);
    }

    @PostMapping("/batch-publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> batchPublishJobs(@RequestBody List<Job> jobs) {
        Long adminId = SecurityUtil.getCurrentUserId();
        return jobService.batchPublishJobs(jobs, adminId);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public Result<Boolean> toggleJobStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (!canManageJob(id)) {
            return Result.error("无权操作该职位");
        }
        Integer role = SecurityUtil.getCurrentRole();
        if (role != null && role == 3) {
            if (status != Job.STATUS_PUBLISHED && status != Job.STATUS_OFFLINE) {
                return Result.error("企业只能上架或下架职位");
            }
            Job existing = jobService.getById(id);
            if (existing == null) {
                return Result.error("职位不存在");
            }
            if (existing.getStatus() != Job.STATUS_PUBLISHED && existing.getStatus() != Job.STATUS_OFFLINE) {
                return Result.error("待审核或未通过职位不能直接上下架");
            }
            Company company = companyMapper.selectByUserId(SecurityUtil.getCurrentUserId());
            if (company == null || company.getStatus() == null || company.getStatus() != Company.STATUS_APPROVED) {
                return Result.error("企业审核通过后才能上架职位");
            }
        }
        return jobService.toggleJobStatus(id, status);
    }

    private boolean canManageJob(Long jobId) {
        Integer role = SecurityUtil.getCurrentRole();
        if (role != null && role == 1) {
            return true;
        }
        if (role == null || role != 3) {
            return false;
        }
        Company company = companyMapper.selectByUserId(SecurityUtil.getCurrentUserId());
        if (company == null) {
            return false;
        }
        Job existing = jobService.getById(jobId);
        return existing != null && company.getId().equals(existing.getCompanyId());
    }
}
