package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.dto.Result;
import com.recruitment.entity.Resume;
import com.recruitment.mapper.ResumeMapper;
import com.recruitment.service.ResumeService;
import com.recruitment.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public Result<Resume> getResumeInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, userId);
        Resume resume = resumeMapper.selectOne(wrapper);
        
        return Result.success(resume);
    }

    @Override
    public Result<Boolean> saveResume(Resume resume) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 查找是否已有简历
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, userId);
        Resume existing = resumeMapper.selectOne(wrapper);
        
        if (existing == null) {
            // 新建简历
            resume.setUserId(userId);
            resume.setIsDefault(Resume.IS_DEFAULT_YES);
            resumeMapper.insert(resume);
        } else {
            // 更新简历
            resume.setId(existing.getId());
            resume.setUserId(userId);
            resumeMapper.updateById(resume);
        }
        
        return Result.success(true);
    }
}
