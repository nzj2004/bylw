package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recruitment.dto.PageResult;
import com.recruitment.dto.Result;
import com.recruitment.entity.Job;
import com.recruitment.entity.JobFavorite;
import com.recruitment.mapper.JobFavoriteMapper;
import com.recruitment.mapper.JobMapper;
import com.recruitment.service.JobFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobFavoriteServiceImpl extends ServiceImpl<JobFavoriteMapper, JobFavorite> implements JobFavoriteService {

    @Autowired
    private JobMapper jobMapper;

    @Override
    public Result<Boolean> addFavorite(Long userId, Long jobId) {
        // 检查是否已收藏
        LambdaQueryWrapper<JobFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobFavorite::getUserId, userId)
               .eq(JobFavorite::getJobId, jobId);
        
        if (baseMapper.selectCount(wrapper) > 0) {
            return Result.error("已收藏该职位");
        }
        
        JobFavorite favorite = new JobFavorite();
        favorite.setUserId(userId);
        favorite.setJobId(jobId);
        baseMapper.insert(favorite);
        
        return Result.success(true);
    }

    @Override
    public Result<Boolean> removeFavorite(Long userId, Long jobId) {
        LambdaQueryWrapper<JobFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobFavorite::getUserId, userId)
               .eq(JobFavorite::getJobId, jobId);
        baseMapper.delete(wrapper);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> isFavorite(Long userId, Long jobId) {
        LambdaQueryWrapper<JobFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobFavorite::getUserId, userId)
               .eq(JobFavorite::getJobId, jobId);
        return Result.success(baseMapper.selectCount(wrapper) > 0);
    }

    @Override
    public Result<PageResult<Job>> listFavorites(Long userId, Long current, Long size) {
        // 查询收藏记录
        LambdaQueryWrapper<JobFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobFavorite::getUserId, userId)
               .orderByDesc(JobFavorite::getCreateTime);
        
        Page<JobFavorite> page = new Page<>(current, size);
        baseMapper.selectPage(page, wrapper);
        
        // 如果没有收藏记录，直接返回空列表
        if (page.getRecords().isEmpty()) {
            return Result.success(PageResult.of(0L, current, size, List.of()));
        }
        
        // 获取职位ID列表
        List<Long> jobIds = page.getRecords().stream()
                .map(JobFavorite::getJobId)
                .collect(Collectors.toList());
        
        // 查询职位详情
        List<Job> jobs = jobMapper.selectBatchIds(jobIds);
        
        return Result.success(PageResult.of(page.getTotal(), current, size, jobs));
    }
}
