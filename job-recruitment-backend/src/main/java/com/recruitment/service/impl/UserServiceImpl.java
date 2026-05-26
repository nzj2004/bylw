package com.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recruitment.dto.*;
import com.recruitment.entity.User;
import com.recruitment.mapper.UserMapper;
import com.recruitment.service.UserService;
import com.recruitment.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Result<String> login(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );

            User user = baseMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, loginDTO.getUsername())
            );

            if (user == null || user.getStatus() == 0) {
                return Result.error("用户已被禁用");
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            return Result.success(token);
        } catch (Exception e) {
            return Result.error("用户名或密码错误");
        }
    }

    @Override
    public Result<String> register(RegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.error("两次密码输入不一致");
        }

        // 检查用户名是否存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (baseMapper.selectCount(usernameWrapper) > 0) {
            return Result.error("用户名已存在");
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(registerDTO.getEmail())) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, registerDTO.getEmail());
            if (baseMapper.selectCount(emailWrapper) > 0) {
                return Result.error("邮箱已被注册");
            }
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(registerDTO.getPhone())) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, registerDTO.getPhone());
            if (baseMapper.selectCount(phoneWrapper) > 0) {
                return Result.error("手机号已被注册");
            }
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : User.ROLE_USER);
        user.setStatus(1);

        try {
            baseMapper.insert(user);
        } catch (Exception e) {
            // 捕获数据库异常，提供更友好的错误提示
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Result.error("注册失败：该用户信息已存在，请更换用户名、邮箱或手机号");
            }
            return Result.error("注册失败：" + e.getMessage());
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Result.success(token);
    }

    @Override
    public Result<Boolean> createUserByAdmin(RegisterDTO registerDTO) {
        // 验证必填字段
        if (!StringUtils.hasText(registerDTO.getUsername())) {
            return Result.error("用户名不能为空");
        }
        if (!StringUtils.hasText(registerDTO.getPassword())) {
            return Result.error("密码不能为空");
        }
        if (registerDTO.getRole() == null) {
            return Result.error("角色不能为空");
        }

        // 检查用户名是否存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (baseMapper.selectCount(usernameWrapper) > 0) {
            return Result.error("用户名已存在");
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(registerDTO.getEmail())) {
            LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(User::getEmail, registerDTO.getEmail());
            if (baseMapper.selectCount(emailWrapper) > 0) {
                return Result.error("邮箱已被注册");
            }
        }

        // 检查手机号是否存在
        if (StringUtils.hasText(registerDTO.getPhone())) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, registerDTO.getPhone());
            if (baseMapper.selectCount(phoneWrapper) > 0) {
                return Result.error("手机号已被注册");
            }
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(registerDTO.getRole());
        user.setStatus(1);

        try {
            baseMapper.insert(user);
            return Result.success(true);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Result.error("创建失败：该用户信息已存在");
            }
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @Override
    public Result<UserDTO> getCurrentUserInfo(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(convertToDTO(user));
    }

    @Override
    public Result<PageResult<UserDTO>> listUsers(String keyword, Integer role, Integer status, Long current, Long size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（用户名、真实姓名、邮箱）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> page = new Page<>(current, size);
        baseMapper.selectPage(page, wrapper);
        
        List<UserDTO> records = page.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return Result.success(PageResult.of(page.getTotal(), current, size, records));
    }

    @Override
    public Result<Boolean> updateUserStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        baseMapper.updateById(user);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> deleteUser(Long id) {
        baseMapper.deleteById(id);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> resetPassword(Long id) {
        User user = baseMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 默认密码：用户名 + 123456
        String defaultPassword = user.getUsername() + "123456";
        user.setPassword(passwordEncoder.encode(defaultPassword));
        baseMapper.updateById(user);
        
        return Result.success(true);
    }

    @Override
    public Result<Boolean> changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        if (userId == null) {
            return Result.error("用户未登录");
        }
        if (changePasswordDTO == null) {
            return Result.error("密码信息不能为空");
        }
        if (!StringUtils.hasText(changePasswordDTO.getOldPassword())) {
            return Result.error("原密码不能为空");
        }
        if (!StringUtils.hasText(changePasswordDTO.getNewPassword())) {
            return Result.error("新密码不能为空");
        }
        if (changePasswordDTO.getNewPassword().length() < 6) {
            return Result.error("新密码至少6位");
        }
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            return Result.error("两次输入的新密码不一致");
        }

        User user = baseMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误");
        }
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            return Result.error("新密码不能与原密码相同");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        baseMapper.updateById(user);
        return Result.success(true);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setRoleName(user.getRoleName());
        if (user.getCreateTime() != null) {
            dto.setCreateTime(user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return dto;
    }
}
