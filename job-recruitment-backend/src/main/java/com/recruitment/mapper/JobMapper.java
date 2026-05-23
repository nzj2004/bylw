package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface JobMapper extends BaseMapper<Job> {

    @Update("UPDATE job_info SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE job_info SET apply_count = apply_count + 1 WHERE id = #{id}")
    void incrementApplyCount(@Param("id") Long id);

    @Select("SELECT j.*, c.company_name as companyName, c.logo_url as companyLogo " +
            "FROM job_info j LEFT JOIN company_info c ON j.company_id = c.id " +
            "WHERE j.id = #{id} AND j.deleted = 0")
    Job selectJobWithCompany(@Param("id") Long id);

    /**
     * 根据关键字搜索职位ID（包括职位标题、描述、公司名称）
     */
    @Select("SELECT DISTINCT j.id FROM job_info j " +
            "LEFT JOIN company_info c ON j.company_id = c.id " +
            "WHERE j.deleted = 0 AND j.status = 1 " +
            "AND (j.title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR j.job_desc LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.company_name LIKE CONCAT('%', #{keyword}, '%'))")
    List<Long> selectJobIdsByKeyword(@Param("keyword") String keyword);
}
