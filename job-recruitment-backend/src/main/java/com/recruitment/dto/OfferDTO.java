package com.recruitment.dto;

import lombok.Data;

@Data
public class OfferDTO {

    private Long id;
    private Long applicationId;
    private String offerTitle;
    private Integer status;
    private String statusName;
    private Integer salaryMin;
    private Integer salaryMax;
    private String benefits;
    private String workLocation;
    private String offerContent;
    private String entryDate;
    private String expireTime;
    private String responseTime;
    private String responseComment;
    private String createTime;
    private String updateTime;
    private String jobTitle;
    private String companyName;
}
