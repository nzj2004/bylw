package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("application")
public class Application extends BaseEntity {

    private Long jobId;
    private Long resumeId;
    private Long userId;
    private Long companyId;
    private Integer status;
    private String remark;
    private LocalDateTime applyTime;
    private LocalDateTime handleTime;

    // 关联查询字段（非数据库字段）
    @TableField(exist = false)
    private String jobTitle;
    
    @TableField(exist = false)
    private String companyName;
    
    @TableField(exist = false)
    private String resumeName;
    
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String offerStatusName;

    private LocalDateTime viewedAt;
    private Long viewedBy;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_VIEWED = 1;
    public static final int STATUS_INTERESTED = 2;
    public static final int STATUS_UNSUITABLE = 3;
    public static final int STATUS_INTERVIEWING = 4;
    public static final int STATUS_INTERVIEW_FAILED = 5;
    public static final int STATUS_OFFER_PENDING = 6;
    public static final int STATUS_OFFER_ACCEPTED = 7;
    public static final int STATUS_OFFER_REJECTED = 8;
    public static final int STATUS_ONBOARDED = 9;
    public static final int STATUS_INTERVIEW_PASSED = 10;
    public static final int STATUS_OFFER_EXPIRED = 11;

    public String getStatusName() {
        return switch (status) {
            case STATUS_PENDING -> "待查看";
            case STATUS_VIEWED -> "已查看";
            case STATUS_INTERESTED -> "感兴趣";
            case STATUS_UNSUITABLE -> "不合适";
            case STATUS_INTERVIEWING -> "面试中";
            case STATUS_INTERVIEW_FAILED -> "面试未通过";
            case STATUS_OFFER_PENDING -> "Offer待确认";
            case STATUS_OFFER_ACCEPTED -> "Offer已接受";
            case STATUS_OFFER_REJECTED -> "Offer已拒绝";
            case STATUS_ONBOARDED -> "已入职";
            case STATUS_INTERVIEW_PASSED -> "面试通过";
            case STATUS_OFFER_EXPIRED -> "Offer已过期";
            default -> "未知";
        };
    }
}
