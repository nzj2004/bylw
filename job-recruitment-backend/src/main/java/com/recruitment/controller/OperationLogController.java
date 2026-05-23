package com.recruitment.controller;

import com.recruitment.dto.PageResult;
import com.recruitment.dto.Result;
import com.recruitment.entity.OperationLog;
import com.recruitment.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询日志列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<OperationLog>> listLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        return operationLogService.listLogs(username, operationType, operationModule, status, startTime, endTime, current, size);
    }

    /**
     * 清空日志
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> clearLogs() {
        return operationLogService.clearLogs();
    }
}
