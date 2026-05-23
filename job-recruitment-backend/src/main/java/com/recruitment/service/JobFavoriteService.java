package com.recruitment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recruitment.dto.PageResult;
import com.recruitment.dto.Result;
import com.recruitment.entity.JobFavorite;
import com.recruitment.entity.Job;

public interface JobFavoriteService extends IService<JobFavorite> {

    /**
     * 收藏职位
     */
    Result<Boolean> addFavorite(Long userId, Long jobId);

    /**
     * 取消收藏
     */
    Result<Boolean> removeFavorite(Long userId, Long jobId);

    /**
     * 查询是否已收藏
     */
    Result<Boolean> isFavorite(Long userId, Long jobId);

    /**
     * 获取用户的收藏列表
     */
    Result<PageResult<Job>> listFavorites(Long userId, Long current, Long size);
}
