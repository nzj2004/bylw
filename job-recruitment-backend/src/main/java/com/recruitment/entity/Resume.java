package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resume")
public class Resume extends BaseEntity {

    private Long userId;
    private String realName;
    private Integer gender;
    private LocalDate birthDate;
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
    private String avatarUrl;  // 个人照片
    private String pdfUrl;     // PDF简历
    private Integer isDefault;

    public static final int GENDER_FEMALE = 0;
    public static final int GENDER_MALE = 1;

    public static final int IS_DEFAULT_NO = 0;
    public static final int IS_DEFAULT_YES = 1;

    public String getGenderName() {
        return gender != null && gender == GENDER_MALE ? "男" : "女";
    }
}
