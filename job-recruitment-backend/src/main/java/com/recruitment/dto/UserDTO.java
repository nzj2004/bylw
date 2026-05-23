package com.recruitment.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Integer role;
    private String roleName;
    private Integer status;
    private String avatar;
    private String createTime;
}
