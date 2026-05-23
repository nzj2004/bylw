package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recruitment.dto.PageResult;
import com.recruitment.dto.Result;
import com.recruitment.entity.OperationLog;
import com.recruitment.mapper.OperationLogMapper;
import com.recruitment.service.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void saveLog(OperationLog log) {
        this.save(log);
    }

    @Override
    public Result<PageResult<OperationLog>> listLogs(String username, String operationType, 
                                                      String operationModule, Integer status,
                                                      String startTime, String endTime,
                                                      Long current, Long size) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 用户名模糊查询
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        
        // 操作类型筛选
        if (StringUtils.hasText(operationType)) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        
        // 操作模块筛选
        if (StringUtils.hasText(operationModule)) {
            wrapper.like(OperationLog::getOperationModule, operationModule);
        }
        
        // 状态筛选
        if (status != null) {
            wrapper.eq(OperationLog::getStatus, status);
        }
        
        // 时间范围筛选
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(OperationLog::getCreateTime, endTime);
        }
        
        // 按时间倒序
        wrapper.orderByDesc(OperationLog::getCreateTime);
        
        Page<OperationLog> page = new Page<>(current, size);
        this.page(page, wrapper);
        
        return Result.success(PageResult.of(page.getTotal(), current, size, page.getRecords()));
    }

    @Override
    public Result<Boolean> clearLogs() {
        this.remove(new LambdaQueryWrapper<>());
        return Result.success(true);
    }
}
