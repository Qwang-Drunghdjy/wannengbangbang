package com.uang.backend.service;

import com.uang.backend.config.JwtUtil;
import com.uang.backend.dto.LoginRequest;
import com.uang.backend.dto.LoginResponse;
import com.uang.backend.dto.RegisterRequest;
import com.uang.backend.entity.User;
import com.uang.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户业务逻辑层：注册 / 登录 / 按 ID 查询
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }

    /**
     * 注册新用户
     * @param request 注册请求（phone/password/nickname）
     * @return 保存后的用户
     * @throws RuntimeException 字段为空或手机号已注册
     */
    public User register(RegisterRequest request) {
        if (!StringUtils.hasText(request.getPhone())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
        if (!StringUtils.hasText(request.getNickname())) {
            throw new RuntimeException("昵称不能为空");
        }
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("手机号已注册");
        }

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(User.Status.NORMAL);
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * 登录：校验手机号 + 密码，成功生成 JWT
     * @param request 登录请求（phone/password）
     * @return LoginResponse（token/userId/nickname）
     * @throws RuntimeException 手机号或密码错误 / 账号被封禁
     */
    public LoginResponse login(LoginRequest request) {
        if (!StringUtils.hasText(request.getPhone()) || !StringUtils.hasText(request.getPassword())) {
            throw new RuntimeException("手机号和密码不能为空");
        }
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("手机号或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("手机号或密码错误");
        }
        if (user.getStatus() == User.Status.BANNED) {
            throw new RuntimeException("账号已被封禁");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return new LoginResponse(token, user.getId(), user.getNickname());
    }

    /**
     * 根据 ID 查询用户
     * @param id 主键
     * @return 用户实体
     * @throws RuntimeException 未找到时抛出
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，id: " + id));
    }
}
