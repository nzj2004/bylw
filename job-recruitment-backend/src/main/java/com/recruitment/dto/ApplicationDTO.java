package com.recruitment.dto;

import lombok.Data;

@Data
public class ApplicationDTO {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private Long resumeId;
    private String resumeName;
    private Long userId;
    private String userName;
    private Long companyId;
    private Integer status;
    private String statusName;
    private String viewedAt;
    private Long viewedBy;
    private String remark;
    private String applyTime;
    private String handleTime;
}
