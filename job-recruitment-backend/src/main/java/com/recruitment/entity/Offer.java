package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("offers")
public class Offer extends BaseEntity {

    private Long applicationId;
    private Integer status;
    private String offerTitle;
    private Integer salaryMin;
    private Integer salaryMax;
    private String benefits;
    private String workLocation;
    private String offerContent;
    private LocalDate entryDate;
    private LocalDateTime expireTime;
    private LocalDateTime responseTime;
    private String responseComment;

    @TableField(exist = false)
    private String jobTitle;

    @TableField(exist = false)
    private String companyName;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPTED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_EXPIRED = 3;

    public String getStatusName() {
        return switch (status) {
            case STATUS_PENDING -> "待确认";
            case STATUS_ACCEPTED -> "已接受";
            case STATUS_REJECTED -> "已拒绝";
            case STATUS_EXPIRED -> "已过期";
            default -> "未知";
        };
    }
}
