package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interview_rounds")
public class InterviewRound extends BaseEntity {

    private Long applicationId;
    private Integer roundNo;
    private String interviewType;
    private LocalDateTime interviewTime;
    private String location;
    private String meetingLink;
    private String contactPerson;
    private String contactPhone;
    private Integer result;
    private String notes;
    private Integer confirmationStatus;
    private LocalDateTime candidateResponseTime;
    private LocalDateTime rescheduleTime;
    private String rescheduleReason;

    public static final int RESULT_PENDING = 0;
    public static final int RESULT_PASS = 1;
    public static final int RESULT_FAIL = 2;
    public static final int RESULT_CANCELLED = 3;

    public static final int CONFIRMATION_PENDING = 0;
    public static final int CONFIRMATION_CONFIRMED = 1;
    public static final int CONFIRMATION_RESCHEDULE_REQUESTED = 2;

    public String getResultName() {
        return switch (result) {
            case RESULT_PENDING -> "待安排";
            case RESULT_PASS -> "通过";
            case RESULT_FAIL -> "未通过";
            case RESULT_CANCELLED -> "已取消";
            default -> "未知";
        };
    }

    public String getConfirmationStatusName() {
        return switch (confirmationStatus == null ? CONFIRMATION_PENDING : confirmationStatus) {
            case CONFIRMATION_PENDING -> "待确认";
            case CONFIRMATION_CONFIRMED -> "已确认";
            case CONFIRMATION_RESCHEDULE_REQUESTED -> "申请改期";
            default -> "未知";
        };
    }
}
