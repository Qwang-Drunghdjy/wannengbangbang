package com.uang.backend.controller;

import com.uang.backend.dto.LoginRequest;
import com.uang.backend.dto.LoginResponse;
import com.uang.backend.dto.RegisterRequest;
import com.uang.backend.dto.Result;
import com.uang.backend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证 RESTful API 控制器（注册 / 登录，公开接口）
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册新用户
     * POST /api/v1/auth/register
     * @param request 注册请求体 { phone, password, nickname }
     * @return 成功响应（code 200）
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success(null);
    }

    /**
     * 登录
     * POST /api/v1/auth/login
     * @param request 登录请求体 { phone, password }
     * @return 登录响应 { token, userId, nickname }
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }
}
