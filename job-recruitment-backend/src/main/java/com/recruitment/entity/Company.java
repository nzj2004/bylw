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
    private String pendingCompanyName;
    private String pendingIndustry;
    private String pendingScale;
    private String pendingAddress;
    private String pendingDescription;
    private String pendingLogoUrl;
    private String pendingWebsite;
    private String pendingContactName;
    private String pendingContactPhone;
    private String pendingContactEmail;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    public boolean hasPendingChanges() {
        return pendingCompanyName != null
                || pendingIndustry != null
                || pendingScale != null
                || pendingAddress != null
                || pendingDescription != null
                || pendingLogoUrl != null
                || pendingWebsite != null
                || pendingContactName != null
                || pendingContactPhone != null
                || pendingContactEmail != null;
    }

    public String getStatusName() {
        return switch (status) {
            case STATUS_PENDING -> "\u5f85\u5ba1\u6838";
            case STATUS_APPROVED -> "\u5df2\u901a\u8fc7";
            case STATUS_REJECTED -> "\u672a\u901a\u8fc7";
            default -> "\u672a\u77e5";
        };
    }
}
