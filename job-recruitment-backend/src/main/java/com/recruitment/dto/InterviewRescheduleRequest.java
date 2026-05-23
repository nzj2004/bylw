package com.recruitment.dto;

import lombok.Data;

@Data
public class InterviewRescheduleRequest {
    private String rescheduleTime;
    private String rescheduleReason;
}
