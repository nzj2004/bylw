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
    private String createTime;
}
