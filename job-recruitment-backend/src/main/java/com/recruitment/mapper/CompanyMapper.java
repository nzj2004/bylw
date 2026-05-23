package com.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruitment.entity.Company;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CompanyMapper extends BaseMapper<Company> {

    @Select("SELECT * FROM company_info WHERE user_id = #{userId} AND deleted = 0")
    Company selectByUserId(Long userId);
}
