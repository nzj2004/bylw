package com.recruitment.service;

import com.recruitment.entity.ApplicationStatusHistory;
import com.recruitment.entity.User;
import com.recruitment.mapper.ApplicationStatusHistoryMapper;
import com.recruitment.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ApplicationStatusHistoryService {

    @Autowired
    private ApplicationStatusHistoryMapper historyMapper;

    public void recordStatusChange(Long applicationId, Integer oldStatus, Integer newStatus, String action, String remark) {
        if (applicationId == null || newStatus == null || Objects.equals(oldStatus, newStatus)) {
            return;
        }
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplicationId(applicationId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(SecurityUtil.getCurrentUserId());
        history.setChangedRole(resolveRoleName(SecurityUtil.getCurrentRole()));
        history.setAction(action);
        history.setRemark(remark);
        historyMapper.insert(history);
    }

    private String resolveRoleName(Integer role) {
        if (role == null) {
            return "SYSTEM";
        }
        return switch (role) {
            case User.ROLE_ADMIN -> "管理员";
            case User.ROLE_OPERATOR -> "运营";
            case User.ROLE_COMPANY -> "企业";
            case User.ROLE_USER -> "求职者";
            default -> "未知";
        };
    }
}
