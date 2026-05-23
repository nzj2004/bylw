package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.entity.ApplicationStatusHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApplicationStatusHistoryMapper extends BaseMapper<ApplicationStatusHistory> {

    @Select("SELECT * FROM application_status_history " +
            "WHERE application_id = #{applicationId} AND deleted = 0 " +
            "ORDER BY create_time DESC, id DESC")
    List<ApplicationStatusHistory> selectByApplicationId(@Param("applicationId") Long applicationId);

    @Insert("INSERT INTO application_status_history " +
            "(application_id, old_status, new_status, changed_by, changed_role, action, remark, create_time, update_time, deleted) " +
            "SELECT a.id, a.status, #{expiredApplicationStatus}, NULL, 'SYSTEM', 'OFFER_EXPIRED', " +
            "'Offer超过有效期自动过期', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0 " +
            "FROM application a " +
            "INNER JOIN offers o ON o.application_id = a.id AND o.deleted = 0 " +
            "WHERE a.deleted = 0 " +
            "AND a.status = #{offerPendingApplicationStatus} " +
            "AND o.status = #{pendingOfferStatus} " +
            "AND o.expire_time IS NOT NULL " +
            "AND o.expire_time < CURRENT_TIMESTAMP")
    int insertExpiredOfferHistories(@Param("offerPendingApplicationStatus") Integer offerPendingApplicationStatus,
                                    @Param("expiredApplicationStatus") Integer expiredApplicationStatus,
                                    @Param("pendingOfferStatus") Integer pendingOfferStatus);
}
