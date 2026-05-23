package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.entity.Offer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OfferMapper extends BaseMapper<Offer> {

    @Select("SELECT * FROM offers WHERE application_id = #{applicationId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    Offer selectLatestByApplicationId(@Param("applicationId") Long applicationId);

    @Select("SELECT * FROM offers WHERE id = #{id} AND deleted = 0")
    Offer selectByIdAndNotDeleted(@Param("id") Long id);

    @Select("SELECT o.id, o.application_id, o.status, o.offer_title, o.salary_min, o.salary_max, o.benefits, " +
            "o.work_location, o.offer_content, o.entry_date, o.expire_time, o.response_time, o.response_comment, " +
            "o.create_time, o.update_time, o.deleted, j.title AS jobTitle, c.company_name AS companyName " +
            "FROM offers o " +
            "INNER JOIN application a ON o.application_id = a.id " +
            "LEFT JOIN job_info j ON a.job_id = j.id " +
            "LEFT JOIN company_info c ON a.company_id = c.id " +
            "WHERE a.user_id = #{userId} AND o.deleted = 0 AND a.deleted = 0 " +
            "ORDER BY o.create_time DESC")
    List<Offer> selectByUserIdWithMeta(@Param("userId") Long userId);

    @Update("UPDATE offers " +
            "SET status = #{expiredStatus}, update_time = CURRENT_TIMESTAMP " +
            "WHERE deleted = 0 " +
            "AND status = #{pendingStatus} " +
            "AND expire_time IS NOT NULL " +
            "AND expire_time < CURRENT_TIMESTAMP")
    int expirePendingOffers(@Param("pendingStatus") Integer pendingStatus,
                            @Param("expiredStatus") Integer expiredStatus);
}
