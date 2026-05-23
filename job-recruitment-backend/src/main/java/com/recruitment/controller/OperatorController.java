package com.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.dto.CompanyDTO;
import com.recruitment.dto.PageResult;
import com.recruitment.dto.Result;
import com.recruitment.entity.Company;
import com.recruitment.entity.Job;
import com.recruitment.mapper.CompanyMapper;
import com.recruitment.mapper.JobMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/operator")
@PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
public class OperatorController {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private JobMapper jobMapper;

    /**
     * 获取企业列表（支持状态筛选）
     */
    @GetMapping("/companies")
    public Result<PageResult<CompanyDTO>> listCompanies(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null) {
            if (status == Company.STATUS_PENDING) {
                wrapper.and(w -> w.eq(Company::getStatus, Company.STATUS_PENDING)
                        .or()
                        .isNotNull(Company::getPendingCompanyName)
                        .or()
                        .isNotNull(Company::getPendingIndustry)
                        .or()
                        .isNotNull(Company::getPendingScale)
                        .or()
                        .isNotNull(Company::getPendingAddress)
                        .or()
                        .isNotNull(Company::getPendingDescription)
                        .or()
                        .isNotNull(Company::getPendingLogoUrl)
                        .or()
                        .isNotNull(Company::getPendingWebsite)
                        .or()
                        .isNotNull(Company::getPendingContactName)
                        .or()
                        .isNotNull(Company::getPendingContactPhone)
                        .or()
                        .isNotNull(Company::getPendingContactEmail));
            } else {
                wrapper.eq(Company::getStatus, status);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Company::getCreateTime);
        
        Page<Company> page = new Page<>(current, size);
        companyMapper.selectPage(page, wrapper);
        
        List<CompanyDTO> records = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return Result.success(PageResult.of(page.getTotal(), current, size, records));
    }

    /**
     * 审核企业
     */
    @PutMapping("/company/{id}/audit")
    public Result<Boolean> auditCompany(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        
        Company company = companyMapper.selectById(id);
        if (company == null) {
            return Result.error("企业不存在");
        }
        
        if (status != Company.STATUS_APPROVED && status != Company.STATUS_REJECTED) {
            return Result.error("审核状态不正确");
        }
        
        if (status == Company.STATUS_REJECTED && (reason == null || reason.trim().isEmpty())) {
            return Result.error("拒绝时必须填写原因");
        }
        
        boolean approvedCompanyInfoChange = company.getStatus() != null
                && company.getStatus() == Company.STATUS_APPROVED
                && company.hasPendingChanges();

        if (status == Company.STATUS_APPROVED) {
            approveCompany(company);
        } else if (approvedCompanyInfoChange) {
            rejectCompanyInfoChange(id, reason);
        } else {
            Company updateCompany = new Company();
            updateCompany.setId(id);
            updateCompany.setStatus(status);
            updateCompany.setRejectReason(reason);
            companyMapper.updateById(updateCompany);
        }

        if (status != Company.STATUS_APPROVED && !approvedCompanyInfoChange) {
            offlineCompanyJobs(id);
        }
        return Result.success(true);
    }

    /**
     * 转换为DTO
     */
    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        BeanUtils.copyProperties(company, dto);
        dto.setHasPendingChanges(company.hasPendingChanges());
        if (company.hasPendingChanges()) {
            applyPendingDisplay(company, dto);
        }
        dto.setStatusName(company.getStatusName());
        if (company.getCreateTime() != null) {
            dto.setCreateTime(company.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }

    private void approveCompany(Company company) {
        LambdaUpdateWrapper<Company> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Company::getId, company.getId())
                .set(Company::getStatus, Company.STATUS_APPROVED)
                .set(Company::getRejectReason, "");

        if (company.hasPendingChanges()) {
            wrapper.set(Company::getCompanyName, company.getPendingCompanyName())
                    .set(Company::getIndustry, company.getPendingIndustry())
                    .set(Company::getScale, company.getPendingScale())
                    .set(Company::getAddress, company.getPendingAddress())
                    .set(Company::getDescription, company.getPendingDescription())
                    .set(Company::getLogoUrl, company.getPendingLogoUrl())
                    .set(Company::getWebsite, company.getPendingWebsite())
                    .set(Company::getContactName, company.getPendingContactName())
                    .set(Company::getContactPhone, company.getPendingContactPhone())
                    .set(Company::getContactEmail, company.getPendingContactEmail())
                    .set(Company::getPendingCompanyName, null)
                    .set(Company::getPendingIndustry, null)
                    .set(Company::getPendingScale, null)
                    .set(Company::getPendingAddress, null)
                    .set(Company::getPendingDescription, null)
                    .set(Company::getPendingLogoUrl, null)
                    .set(Company::getPendingWebsite, null)
                    .set(Company::getPendingContactName, null)
                    .set(Company::getPendingContactPhone, null)
                    .set(Company::getPendingContactEmail, null);
        }

        companyMapper.update(null, wrapper);
    }

    private void rejectCompanyInfoChange(Long companyId, String reason) {
        LambdaUpdateWrapper<Company> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Company::getId, companyId)
                .set(Company::getStatus, Company.STATUS_APPROVED)
                .set(Company::getRejectReason, reason)
                .set(Company::getPendingCompanyName, null)
                .set(Company::getPendingIndustry, null)
                .set(Company::getPendingScale, null)
                .set(Company::getPendingAddress, null)
                .set(Company::getPendingDescription, null)
                .set(Company::getPendingLogoUrl, null)
                .set(Company::getPendingWebsite, null)
                .set(Company::getPendingContactName, null)
                .set(Company::getPendingContactPhone, null)
                .set(Company::getPendingContactEmail, null);
        companyMapper.update(null, wrapper);
    }

    private void applyPendingDisplay(Company company, CompanyDTO dto) {
        dto.setCompanyName(company.getPendingCompanyName());
        dto.setIndustry(company.getPendingIndustry());
        dto.setScale(company.getPendingScale());
        dto.setAddress(company.getPendingAddress());
        dto.setDescription(company.getPendingDescription());
        dto.setLogoUrl(company.getPendingLogoUrl());
        dto.setWebsite(company.getPendingWebsite());
        dto.setContactName(company.getPendingContactName());
        dto.setContactPhone(company.getPendingContactPhone());
        dto.setContactEmail(company.getPendingContactEmail());
    }

    private void offlineCompanyJobs(Long companyId) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getCompanyId, companyId);
        wrapper.ne(Job::getStatus, Job.STATUS_OFFLINE);
        Job update = new Job();
        update.setStatus(Job.STATUS_OFFLINE);
        jobMapper.update(update, wrapper);
    }
}
