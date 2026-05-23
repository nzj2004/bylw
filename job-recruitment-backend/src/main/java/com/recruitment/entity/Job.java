package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_info")
public class Job extends BaseEntity {

    private Long companyId;
    private String title;
    private String category;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer salaryMonth;
    private String workCity;
    private String workAddress;
    private String experience;
    private String education;
    private Integer jobType;
    private String jobDesc;
    private String requirements;
    private String welfare;
    private Integer status;
    private String rejectReason;
    private Integer viewCount;
    private Integer applyCount;
    private LocalDateTime publishTime;
    private LocalDate deadline;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_OFFLINE = 3;

    public static final int JOB_TYPE_FULLTIME = 1;
    public static final int JOB_TYPE_PARTTIME = 2;
    public static final int JOB_TYPE_INTERNSHIP = 3;

    public String getStatusName() {
        return switch (status) {
            case STATUS_PENDING -> "待审核";
            case STATUS_PUBLISHED -> "已发布";
            case STATUS_REJECTED -> "已拒绝";
            case STATUS_OFFLINE -> "已下线";
            default -> "未知";
        };
    }

    public String getJobTypeName() {
        return switch (jobType) {
            case JOB_TYPE_FULLTIME -> "全职";
            case JOB_TYPE_PARTTIME -> "兼职";
            case JOB_TYPE_INTERNSHIP -> "实习";
            default -> "未知";
        };
    }

    public String getSalaryRange() {
        if (salaryMin != null && salaryMax != null) {
            return salaryMin + "-" + salaryMax + "K";
        } else if (salaryMin != null) {
            return salaryMin + "K以上";
        } else if (salaryMax != null) {
            return salaryMax + "K以下";
        }
        return "薪资面议";
    }
}
