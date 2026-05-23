package com.recruitment.controller;

import com.recruitment.dto.LoginDTO;
import com.recruitment.dto.RegisterDTO;
import com.recruitment.dto.Result;
import com.recruitment.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Result<String> tokenResult = userService.login(loginDTO);
        if (tokenResult.getCode() != 200) {
            return Result.error(tokenResult.getCode(), tokenResult.getMessage());
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", tokenResult.getData());
        data.put("tokenType", "Bearer");
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO registerDTO) {
        Result<String> tokenResult = userService.register(registerDTO);
        if (tokenResult.getCode() != 200) {
            return Result.error(tokenResult.getCode(), tokenResult.getMessage());
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", tokenResult.getData());
        data.put("tokenType", "Bearer");
        return Result.success(data);
    }
}
