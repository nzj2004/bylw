package com.recruitment.controller;

import com.recruitment.dto.PageResult;
import com.recruitment.dto.Result;
import com.recruitment.entity.Job;
import com.recruitment.service.JobFavoriteService;
import com.recruitment.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite")
public class JobFavoriteController {

    @Autowired
    private JobFavoriteService jobFavoriteService;

    /**
     * 收藏职位
     */
    @PostMapping("/{jobId}")
    public Result<Boolean> addFavorite(@PathVariable Long jobId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return jobFavoriteService.addFavorite(userId, jobId);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{jobId}")
    public Result<Boolean> removeFavorite(@PathVariable Long jobId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return jobFavoriteService.removeFavorite(userId, jobId);
    }

    /**
     * 查询是否已收藏
     */
    @GetMapping("/{jobId}/status")
    public Result<Boolean> isFavorite(@PathVariable Long jobId) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return jobFavoriteService.isFavorite(userId, jobId);
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/list")
    public Result<PageResult<Job>> listFavorites(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return jobFavoriteService.listFavorites(userId, current, size);
    }
}
