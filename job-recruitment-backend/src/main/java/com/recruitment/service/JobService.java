package com.recruitment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recruitment.dto.*;
import com.recruitment.entity.Job;

import java.util.List;

public interface JobService extends IService<Job> {

    Result<Boolean> createJob(Job job, Long companyId);

    Result<Boolean> updateJob(Job job);

    Result<Boolean> deleteJob(Long id);

    Result<JobDTO> getJobById(Long id);

    Result<PageResult<JobDTO>> listJobs(String keyword, String category, String city, 
                                         String experience, String education,
                                         Integer minSalary, Integer maxSalary, 
                                         Integer status, Boolean includeAll, Long current, Long size);

    Result<PageResult<JobDTO>> listHotJobs(Long current, Long size);

    Result<PageResult<JobDTO>> listCompanyJobs(Long companyId, String keyword, String category, Integer status, Long current, Long size);

    Result<Boolean> auditJob(Long id, Integer status, String reason);

    Result<Boolean> batchAuditJobs(List<Long> jobIds, Integer status, String reason);

    Result<Boolean> batchPublishJobs(List<Job> jobs, Long adminId);

    Result<Boolean> incrementViewCount(Long id);

    Result<Boolean> incrementApplyCount(Long id);

    Result<Boolean> toggleJobStatus(Long id, Integer status);
}
