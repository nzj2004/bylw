package com.recruitment.dto;

import lombok.Data;

@Data
public class ResumeDTO {

    private Long id;
    private Long userId;
    private String realName;
    private Integer gender;
    private String genderName;
    private String birthDate;
    private String phone;
    private String email;
    private String education;
    private String school;
    private String major;
    private Integer graduationYear;
    private String workExperience;
    private String projectExp;
    private String selfEval;
    private String skills;
    private String expectedCity;
    private Integer expectedSalaryMin;
    private Integer expectedSalaryMax;
    private String attachmentUrl;
    private Integer isDefault;
    private String createTime;
}
