package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("application_status_history")
public class ApplicationStatusHistory extends BaseEntity {

    private Long applicationId;
    private Integer oldStatus;
    private Integer newStatus;
    private Long changedBy;
    private String changedRole;
    private String action;
    private String remark;
}
