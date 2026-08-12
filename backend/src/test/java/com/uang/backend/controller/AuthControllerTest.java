package com.uang.backend.controller;

import com.uang.backend.config.JwtUtil;
import com.uang.backend.dto.LoginRequest;
import com.uang.backend.dto.LoginResponse;
import com.uang.backend.dto.RegisterRequest;
import com.uang.backend.entity.User;
import com.uang.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // @WebMvcTest 会加载 WebMvcConfig + AuthInterceptor，需要 JwtUtil bean
    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void register_shouldReturn200() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(new User());

        String body = """
                {
                    "phone": "13800001111",
                    "password": "123456",
                    "nickname": "张三"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void register_shouldReturnErrorWhenFieldMissing() throws Exception {
        doThrow(new RuntimeException("手机号不能为空"))
                .when(userService).register(any(RegisterRequest.class));

        String body = """
                {
                    "password": "123456",
                    "nickname": "张三"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("手机号不能为空"));
    }

    @Test
    void login_shouldReturnLoginResponse() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("mock-token", 1L, "张三"));

        String body = """
                {
                    "phone": "13800001111",
                    "password": "123456"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-token"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("张三"));
    }
}
