package com.recruitment.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.entity.OperationLog;
import com.recruitment.security.UserDetailsImpl;
import com.recruitment.service.OperationLogService;
import com.recruitment.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点 - 所有Controller方法
     */
    @Pointcut("execution(* com.recruitment.controller.*.*(..))")
    public void controllerPointcut() {}

    /**
     * 环绕通知
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 获取当前用户信息
        Long userId = null;
        String username = "anonymous";
        String roleName = "未知";
        
        try {
            UserDetailsImpl currentUser = SecurityUtil.getCurrentUser();
            if (currentUser != null) {
                userId = currentUser.getId();
                username = currentUser.getUsername();
                roleName = getRoleName(currentUser.getRole());
            }
        } catch (Exception e) {
            // 未登录用户
        }
        
        // 构建日志对象
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(userId);
        operationLog.setUsername(username);
        operationLog.setRoleName(roleName);
        operationLog.setRequestMethod(request.getMethod());
        operationLog.setRequestUrl(request.getRequestURI());
        operationLog.setIpAddress(getIpAddress(request));
        operationLog.setUserAgent(request.getHeader("User-Agent"));
        operationLog.setOperationModule(getOperationModule(request.getRequestURI()));
        operationLog.setOperationType(getOperationType(request.getMethod()));
        operationLog.setOperationDesc(getOperationDesc(method, request.getRequestURI()));
        
        // 记录请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String params = objectMapper.writeValueAsString(args);
                // 限制长度
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
                operationLog.setRequestParams(params);
            }
        } catch (Exception e) {
            operationLog.setRequestParams("参数序列化失败");
        }
        
        Object result = null;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 记录成功
            operationLog.setStatus(OperationLog.STATUS_SUCCESS);
            
            // 记录响应结果
            try {
                String responseStr = objectMapper.writeValueAsString(result);
                if (responseStr.length() > 2000) {
                    responseStr = responseStr.substring(0, 2000) + "...";
                }
                operationLog.setResponseResult(responseStr);
            } catch (Exception e) {
                operationLog.setResponseResult("响应序列化失败");
            }
            
        } catch (Throwable throwable) {
            // 记录失败
            operationLog.setStatus(OperationLog.STATUS_FAIL);
            operationLog.setErrorMsg(throwable.getMessage());
            throw throwable;
        } finally {
            // 计算执行时长
            long executionTime = System.currentTimeMillis() - startTime;
            operationLog.setExecutionTime(executionTime);
            
            // 异步保存日志（避免影响主流程性能）
            try {
                operationLogService.saveLog(operationLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
        
        return result;
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理情况，取第一个IP
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }

    /**
     * 根据URL获取操作模块
     */
    private String getOperationModule(String uri) {
        if (uri.contains("/auth/")) return "认证管理";
        if (uri.contains("/user/")) return "用户管理";
        if (uri.contains("/job/")) return "职位管理";
        if (uri.contains("/company/")) return "企业管理";
        if (uri.contains("/resume/")) return "简历管理";
        if (uri.contains("/application/")) return "投递管理";
        if (uri.contains("/operator/")) return "运营管理";
        if (uri.contains("/log/")) return "日志管理";
        return "其他模块";
    }

    /**
     * 根据HTTP方法获取操作类型
     */
    private String getOperationType(String method) {
        return switch (method.toUpperCase()) {
            case "GET" -> OperationLog.TYPE_QUERY;
            case "POST" -> OperationLog.TYPE_CREATE;
            case "PUT" -> OperationLog.TYPE_UPDATE;
            case "DELETE" -> OperationLog.TYPE_DELETE;
            default -> OperationLog.TYPE_OTHER;
        };
    }

    /**
     * 获取操作描述
     */
    private String getOperationDesc(Method method, String uri) {
        // 可以根据方法名或自定义注解生成描述
        String methodName = method.getName();
        return switch (methodName) {
            case "login" -> "用户登录";
            case "register" -> "用户注册";
            case "logout" -> "用户退出";
            case "list" -> "查询列表";
            case "get" -> "查询详情";
            case "create" -> "创建数据";
            case "update" -> "更新数据";
            case "delete" -> "删除数据";
            default -> methodName;
        };
    }

    /**
     * 根据角色编码获取角色名称
     */
    private String getRoleName(Integer role) {
        if (role == null) return "未知";
        return switch (role) {
            case 1 -> "管理员";
            case 2 -> "运营";
            case 3 -> "企业";
            case 4 -> "求职者";
            default -> "未知";
        };
    }
}
