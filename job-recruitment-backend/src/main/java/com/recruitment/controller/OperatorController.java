package com.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.dto.CompanyDTO;
import com.recruitment.dto.PageResult;
import com.recruitment.dto.Result;
import com.recruitment.entity.Company;
import com.recruitment.mapper.CompanyMapper;
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
            wrapper.eq(Company::getStatus, status);
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
        
        Company updateCompany = new Company();
        updateCompany.setId(id);
        updateCompany.setStatus(status);
        if (status == Company.STATUS_REJECTED) {
            updateCompany.setRejectReason(reason);
        }
        
        companyMapper.updateById(updateCompany);
        return Result.success(true);
    }

    /**
     * 转换为DTO
     */
    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        BeanUtils.copyProperties(company, dto);
        dto.setStatusName(company.getStatusName());
        if (company.getCreateTime() != null) {
            dto.setCreateTime(company.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }
}
