package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("company_info")
public class Company extends BaseEntity {

    private Long userId;
    private String companyName;
    private String industry;
    private String scale;
    private String address;
    private String description;
    private String logoUrl;
    private String website;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private Integer status;
    private String rejectReason;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    public String getStatusName() {
        return switch (status) {
            case STATUS_PENDING -> "待审核";
            case STATUS_APPROVED -> "已通过";
            case STATUS_REJECTED -> "已拒绝";
            default -> "未知";
        };
    }
}
