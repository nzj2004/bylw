package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.entity.Resume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {

    @Select("SELECT * FROM resume WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, create_time DESC")
    List<Resume> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM resume WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0 LIMIT 1")
    Resume selectDefaultByUserId(@Param("userId") Long userId);

    @Update("UPDATE resume SET is_default = 0 WHERE user_id = #{userId}")
    void clearDefaultByUserId(@Param("userId") Long userId);
}
