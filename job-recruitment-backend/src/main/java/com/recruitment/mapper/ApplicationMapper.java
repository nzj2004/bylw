package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.entity.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    @Select("SELECT a.*, j.title as jobTitle, c.company_name as companyName, " +
            "r.real_name as resumeName, u.username as userName " +
            "FROM application a " +
            "LEFT JOIN job_info j ON a.job_id = j.id " +
            "LEFT JOIN company_info c ON a.company_id = c.id " +
            "LEFT JOIN resume r ON a.resume_id = r.id " +
            "LEFT JOIN sys_user u ON a.user_id = u.id " +
            "WHERE a.user_id = #{userId} AND a.deleted = 0 " +
            "ORDER BY a.apply_time DESC")
    List<Application> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT a.*, j.title as jobTitle, c.company_name as companyName, " +
            "r.real_name as resumeName, u.username as userName " +
            "FROM application a " +
            "LEFT JOIN job_info j ON a.job_id = j.id " +
            "LEFT JOIN company_info c ON a.company_id = c.id " +
            "LEFT JOIN resume r ON a.resume_id = r.id " +
            "LEFT JOIN sys_user u ON a.user_id = u.id " +
            "WHERE a.company_id = #{companyId} AND a.deleted = 0 " +
            "ORDER BY a.apply_time DESC")
    List<Application> selectByCompanyId(@Param("companyId") Long companyId);

    @Select("<script>" +
            "SELECT a.*, j.title as jobTitle, j.category as jobCategory, c.company_name as companyName, " +
            "r.real_name as resumeName, u.username as userName " +
            "FROM application a " +
            "LEFT JOIN job_info j ON a.job_id = j.id " +
            "LEFT JOIN company_info c ON a.company_id = c.id " +
            "LEFT JOIN resume r ON a.resume_id = r.id " +
            "LEFT JOIN sys_user u ON a.user_id = u.id " +
            "WHERE a.company_id = #{companyId} AND a.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND j.title LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "<if test='categoryValues != null and categoryValues.size > 0'>" +
            "AND j.category IN " +
            "<foreach collection='categoryValues' item='categoryValue' open='(' separator=',' close=')'>" +
            "#{categoryValue}" +
            "</foreach> " +
            "</if>" +
            "<if test='status != null'>" +
            "AND a.status = #{status} " +
            "</if>" +
            "ORDER BY a.apply_time DESC" +
            "</script>")
    IPage<Application> selectPageByCompanyId(Page<Application> page, @Param("companyId") Long companyId,
                                              @Param("keyword") String keyword, @Param("categoryValues") List<String> categoryValues,
                                              @Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM application WHERE job_id = #{jobId} AND user_id = #{userId} AND deleted = 0")
    int countByJobAndUser(@Param("jobId") Long jobId, @Param("userId") Long userId);

    @Select("SELECT * FROM application WHERE id = #{applicationId} AND company_id = #{companyId} AND deleted = 0")
    Application selectByIdAndCompanyId(@Param("applicationId") Long applicationId, @Param("companyId") Long companyId);

    @Select("SELECT * FROM application WHERE id = #{applicationId} AND user_id = #{userId} AND deleted = 0")
    Application selectByIdAndUserId(@Param("applicationId") Long applicationId, @Param("userId") Long userId);

    @Select("SELECT * FROM application WHERE resume_id = #{resumeId} AND company_id = #{companyId} AND deleted = 0 ORDER BY apply_time DESC LIMIT 1")
    Application selectLatestByResumeAndCompany(@Param("resumeId") Long resumeId, @Param("companyId") Long companyId);

    @Update("UPDATE application " +
            "SET status = #{status}, handle_time = #{handleTime}, viewed_at = #{viewedAt}, viewed_by = #{viewedBy}, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND deleted = 0")
    int markAsViewed(@Param("id") Long id, @Param("status") Integer status,
                     @Param("handleTime") java.time.LocalDateTime handleTime,
                     @Param("viewedAt") java.time.LocalDateTime viewedAt,
                     @Param("viewedBy") Long viewedBy);

    @Update("UPDATE application a " +
            "INNER JOIN offers o ON o.application_id = a.id AND o.deleted = 0 " +
            "SET a.status = #{expiredApplicationStatus}, a.handle_time = CURRENT_TIMESTAMP, a.update_time = CURRENT_TIMESTAMP " +
            "WHERE a.deleted = 0 " +
            "AND a.status = #{offerPendingApplicationStatus} " +
            "AND o.status = #{pendingOfferStatus} " +
            "AND o.expire_time IS NOT NULL " +
            "AND o.expire_time < CURRENT_TIMESTAMP")
    int expireApplicationsForExpiredOffers(@Param("offerPendingApplicationStatus") Integer offerPendingApplicationStatus,
                                           @Param("expiredApplicationStatus") Integer expiredApplicationStatus,
                                           @Param("pendingOfferStatus") Integer pendingOfferStatus);
}
