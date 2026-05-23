package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.entity.InterviewRound;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface InterviewRoundMapper extends BaseMapper<InterviewRound> {

    @Select("SELECT * FROM interview_rounds " +
            "WHERE application_id = #{applicationId} AND deleted = 0 " +
            "ORDER BY round_no ASC, create_time DESC")
    List<InterviewRound> selectByApplicationId(@Param("applicationId") Long applicationId);

    @Select("SELECT * FROM interview_rounds WHERE id = #{id} AND deleted = 0")
    InterviewRound selectByIdIncludeDeleted(@Param("id") Long id);

    @Update("UPDATE interview_rounds SET confirmation_status = #{confirmationStatus}, " +
            "candidate_response_time = CURRENT_TIMESTAMP, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND deleted = 0")
    int updateConfirmation(@Param("id") Long id, @Param("confirmationStatus") Integer confirmationStatus);

    @Update("UPDATE interview_rounds SET confirmation_status = #{confirmationStatus}, " +
            "candidate_response_time = CURRENT_TIMESTAMP, reschedule_time = #{rescheduleTime}, " +
            "reschedule_reason = #{rescheduleReason}, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND deleted = 0")
    int requestReschedule(@Param("id") Long id,
                          @Param("confirmationStatus") Integer confirmationStatus,
                          @Param("rescheduleTime") java.time.LocalDateTime rescheduleTime,
                          @Param("rescheduleReason") String rescheduleReason);

    @Update("UPDATE interview_rounds SET interview_time = #{interviewTime}, confirmation_status = #{confirmationStatus}, " +
            "candidate_response_time = NULL, reschedule_time = NULL, reschedule_reason = NULL, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND deleted = 0")
    int acceptReschedule(@Param("id") Long id,
                         @Param("interviewTime") java.time.LocalDateTime interviewTime,
                         @Param("confirmationStatus") Integer confirmationStatus);
}
