package com.recruitment.dto;

import lombok.Data;

@Data
public class ApplicationStatusHistoryDTO {
    private Long id;
    private Long applicationId;
    private Integer oldStatus;
    private String oldStatusName;
    private Integer newStatus;
    private String newStatusName;
    private Long changedBy;
    private String changedRole;
    private String action;
    private String actionName;
    private String remark;
    private String createTime;
}
