package com.recruitment.dto;

import lombok.Data;

@Data
public class InterviewRoundDTO {

    private Long id;
    private Long applicationId;
    private Integer roundNo;
    private String interviewType;
    private String interviewTime;
    private String location;
    private String meetingLink;
    private String contactPerson;
    private String contactPhone;
    private Integer result;
    private String resultName;
    private String notes;
    private Integer confirmationStatus;
    private String confirmationStatusName;
    private String candidateResponseTime;
    private String rescheduleTime;
    private String rescheduleReason;
    private String createTime;
    private String updateTime;
}
