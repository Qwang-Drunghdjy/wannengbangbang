package com.uang.backend.controller;

import com.uang.backend.config.JwtUtil;
import com.uang.backend.dto.DescribeImageResult;
import com.uang.backend.exception.RateLimitException;
import com.uang.backend.service.AiService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiService aiService;

    // @WebMvcTest 会加载 WebMvcConfig + AuthInterceptor，需要 JwtUtil bean
    @MockitoBean
    private JwtUtil jwtUtil;

    private void mockLogin(Long userId) {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(String.valueOf(userId));
        when(jwtUtil.parseToken("test-token")).thenReturn(claims);
    }

    @Test
    void describe_shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(post("/api/v1/ai/describe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageBase64\":\"data\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或登录已过期"));
    }

    @Test
    void describe_shouldReturnGeneratedResult() throws Exception {
        mockLogin(1L);
        when(aiService.describeImage(any(), eq(1L)))
                .thenReturn(new DescribeImageResult("黑色钱包", "黑色 皮质 拉链 男士"));

        mockMvc.perform(post("/api/v1/ai/describe")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageBase64\":\"aW1hZ2U=\",\"category\":\"seek\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("黑色钱包"))
                .andExpect(jsonPath("$.data.description").value("黑色 皮质 拉链 男士"));
    }

    @Test
    void describe_shouldReturn400WhenImageEmpty() throws Exception {
        mockLogin(1L);

        mockMvc.perform(post("/api/v1/ai/describe")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageBase64\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("图片不能为空"));
    }

    @Test
    void describe_shouldReturn429WhenRateLimited() throws Exception {
        mockLogin(1L);
        when(aiService.describeImage(any(), eq(1L)))
                .thenThrow(new RateLimitException("操作过于频繁，请稍后再试"));

        mockMvc.perform(post("/api/v1/ai/describe")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageBase64\":\"aW1hZ2U=\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").value("操作过于频繁，请稍后再试"));
    }
}
