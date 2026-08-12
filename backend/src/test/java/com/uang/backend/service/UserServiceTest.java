package com.uang.backend.service;

import com.uang.backend.config.JwtUtil;
import com.uang.backend.dto.LoginRequest;
import com.uang.backend.dto.LoginResponse;
import com.uang.backend.dto.RegisterRequest;
import com.uang.backend.entity.User;
import com.uang.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String PHONE = "13800001111";
    private static final String RAW_PASSWORD = "123456";

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, jwtUtil);
    }

    private User sampleUser(String rawPassword) {
        User user = new User();
        user.setId(1L);
        user.setPhone(PHONE);
        user.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
        user.setNickname("张三");
        user.setStatus(User.Status.NORMAL);
        user.setCreateTime(LocalDateTime.now());
        return user;
    }

    // ── register ──────────────────────────────────────────────

    @Test
    void register_shouldEncodePasswordAndSave() {
        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterRequest request = new RegisterRequest(PHONE, RAW_PASSWORD, "张三");
        User saved = service.register(request);

        verify(userRepository).findByPhone(PHONE);
        verify(userRepository).save(any(User.class));
        assertThat(saved.getId()).isNull();
        assertThat(saved.getPhone()).isEqualTo(PHONE);
        // 密码必须 BCrypt 加密存储，不能是明文
        assertThat(saved.getPassword()).isNotEqualTo(RAW_PASSWORD);
        assertThat(new BCryptPasswordEncoder().matches(RAW_PASSWORD, saved.getPassword())).isTrue();
        assertThat(saved.getStatus()).isEqualTo(User.Status.NORMAL);
        assertThat(saved.getCreateTime()).isNotNull();
    }

    @Test
    void register_shouldThrowWhenPhoneExists() {
        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.of(sampleUser(RAW_PASSWORD)));

        RegisterRequest request = new RegisterRequest(PHONE, RAW_PASSWORD, "张三");

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("手机号已注册");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenPhoneBlank() {
        RegisterRequest request = new RegisterRequest("", RAW_PASSWORD, "张三");

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("手机号不能为空");
    }

    // ── login ─────────────────────────────────────────────────

    @Test
    void login_shouldReturnLoginResponse() {
        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.of(sampleUser(RAW_PASSWORD)));
        when(jwtUtil.generateToken(1L, PHONE)).thenReturn("mock-token");

        LoginResponse response = service.login(new LoginRequest(PHONE, RAW_PASSWORD));

        assertThat(response.getToken()).isEqualTo("mock-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getNickname()).isEqualTo("张三");
        verify(jwtUtil).generateToken(1L, PHONE);
    }

    @Test
    void login_shouldThrowWhenPasswordWrong() {
        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.of(sampleUser(RAW_PASSWORD)));

        assertThatThrownBy(() -> service.login(new LoginRequest(PHONE, "wrong-pass")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("手机号或密码错误");
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_shouldThrowWhenPhoneNotFound() {
        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest(PHONE, RAW_PASSWORD)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("手机号或密码错误");
    }

    @Test
    void login_shouldThrowWhenBanned() {
        User banned = sampleUser(RAW_PASSWORD);
        banned.setStatus(User.Status.BANNED);
        when(userRepository.findByPhone(PHONE)).thenReturn(Optional.of(banned));

        assertThatThrownBy(() -> service.login(new LoginRequest(PHONE, RAW_PASSWORD)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("账号已被封禁");
    }

    // ── findById ──────────────────────────────────────────────

    @Test
    void findById_shouldReturnUserWhenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser(RAW_PASSWORD)));

        User result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPhone()).isEqualTo(PHONE);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户不存在");
    }
}
