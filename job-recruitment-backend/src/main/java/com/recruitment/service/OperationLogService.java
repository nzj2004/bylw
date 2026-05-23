package com.recruitment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recruitment.dto.PageResult;
import com.recruitment.dto.Result;
import com.recruitment.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {

    /**
     * 保存操作日志
     */
    void saveLog(OperationLog log);

    /**
     * 分页查询日志列表
     */
    Result<PageResult<OperationLog>> listLogs(String username, String operationType, 
                                               String operationModule, Integer status,
                                               String startTime, String endTime,
                                               Long current, Long size);

    /**
     * 清空日志
     */
    Result<Boolean> clearLogs();
}
