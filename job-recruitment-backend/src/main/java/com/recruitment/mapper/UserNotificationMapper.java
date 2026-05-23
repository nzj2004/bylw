package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {

    @Select("SELECT * FROM user_notification WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC, id DESC")
    List<UserNotification> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_notification WHERE user_id = #{userId} AND read_status = 0 AND deleted = 0")
    int countUnread(@Param("userId") Long userId);

    @Update("UPDATE user_notification SET read_status = 1, read_time = CURRENT_TIMESTAMP, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND user_id = #{userId} AND deleted = 0")
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE user_notification SET read_status = 1, read_time = CURRENT_TIMESTAMP, update_time = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND read_status = 0 AND deleted = 0")
    int markAllRead(@Param("userId") Long userId);
}
