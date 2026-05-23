package com.recruitment.dto;

import lombok.Data;

@Data
public class CompanyDTO {

    private Long id;
    private Long userId;
    private String companyName;
    private String industry;
    private String scale;
    private String address;
    private String description;
    private String logoUrl;
    private String website;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Integer status;
    private String statusName;
    private String rejectReason;
    private Boolean hasPendingChanges;
    private String pendingCompanyName;
    private String pendingIndustry;
    private String pendingScale;
    private String pendingAddress;
    private String pendingDescription;
    private String pendingLogoUrl;
    private String pendingWebsite;
    private String pendingContactName;
    private String pendingContactPhone;
    private String pendingContactEmail;
    private String createTime;
}
