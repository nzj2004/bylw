package com.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private Integer role;
    private Integer status;
    private String avatar;

    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_OPERATOR = 2;
    public static final int ROLE_COMPANY = 3;
    public static final int ROLE_USER = 4;

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    public String getRoleName() {
        return switch (role) {
            case ROLE_ADMIN -> "管理员";
            case ROLE_OPERATOR -> "运营";
            case ROLE_COMPANY -> "企业";
            case ROLE_USER -> "求职者";
            default -> "未知";
        };
    }
}
