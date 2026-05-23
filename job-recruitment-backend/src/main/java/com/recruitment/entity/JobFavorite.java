package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_favorite")
public class JobFavorite extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 职位ID
     */
    private Long jobId;
}
