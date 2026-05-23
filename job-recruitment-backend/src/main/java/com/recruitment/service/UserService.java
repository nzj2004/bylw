package com.recruitment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recruitment.dto.*;
import com.recruitment.entity.User;

public interface UserService extends IService<User> {

    Result<String> login(LoginDTO loginDTO);

    Result<String> register(RegisterDTO registerDTO);

    Result<Boolean> createUserByAdmin(RegisterDTO registerDTO);

    Result<UserDTO> getCurrentUserInfo(Long userId);

    Result<PageResult<UserDTO>> listUsers(String keyword, Integer role, Integer status, Long current, Long size);

    Result<Boolean> updateUserStatus(Long id, Integer status);

    Result<Boolean> deleteUser(Long id);

    Result<Boolean> resetPassword(Long id);
}
