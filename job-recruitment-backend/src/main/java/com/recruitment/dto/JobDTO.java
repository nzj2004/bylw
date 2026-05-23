package com.recruitment.dto;

import lombok.Data;

@Data
public class JobDTO {

    private Long id;
    private Long companyId;
    private String companyName;
    private String companyLogo;
    private String title;
    private String category;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer salaryMonth;
    private String salaryRange;
    private String workCity;
    private String workAddress;
    private String experience;
    private String education;
    private Integer jobType;
    private String jobTypeName;
    private String jobDesc;
    private String requirements;
    private String welfare;
    private Integer status;
    private String statusName;
    private Integer viewCount;
    private Integer applyCount;
    private String publishTime;
    private String deadline;
    private String createTime;
}
