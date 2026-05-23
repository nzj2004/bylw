package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operation_log")
public class OperationLog extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色
     */
    private String roleName;

    /**
     * 操作类型：LOGIN/LOGOUT/CREATE/UPDATE/DELETE/QUERY/EXPORT/IMPORT
     */
    private String operationType;

    /**
     * 操作模块
     */
    private String operationModule;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 请求方法：GET/POST/PUT/DELETE
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * 操作结果：0-失败，1-成功
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作IP地址
     */
    private String ipAddress;

    /**
     * 操作地点
     */
    private String ipLocation;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 执行时长（毫秒）
     */
    private Long executionTime;

    // 操作类型常量
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_LOGOUT = "LOGOUT";
    public static final String TYPE_CREATE = "CREATE";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_DELETE = "DELETE";
    public static final String TYPE_QUERY = "QUERY";
    public static final String TYPE_EXPORT = "EXPORT";
    public static final String TYPE_IMPORT = "IMPORT";
    public static final String TYPE_OTHER = "OTHER";

    // 状态常量
    public static final int STATUS_FAIL = 0;
    public static final int STATUS_SUCCESS = 1;
}
