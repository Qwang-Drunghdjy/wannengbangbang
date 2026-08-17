package com.uang.backend.controller;

import com.uang.backend.config.JwtUtil;
import com.uang.backend.entity.LostItem;
import com.uang.backend.service.LostItemService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LostItemController.class)
class LostItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LostItemService service;

    // @WebMvcTest 会加载 WebMvcConfig + AuthInterceptor，需要 JwtUtil bean
    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void create_shouldReturnSavedItem() throws Exception {
        LostItem item = new LostItem();
        item.setId(1L);
        item.setTitle("黑色钱包");
        item.setDescription("在图书馆门口捡到");
        item.setLocation("图书馆");
        item.setContact("13800001111");
        item.setCreateTime(LocalDateTime.now());

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("1");
        when(jwtUtil.parseToken("test-token")).thenReturn(claims);
        when(service.create(any(LostItem.class), eq(1L))).thenReturn(item);

        String body = """
                {
                    "title": "黑色钱包",
                    "description": "在图书馆门口捡到",
                    "location": "图书馆",
                    "contact": "13800001111",
                    "image_url": "https://example.com/img.jpg"
                }
                """;

        mockMvc.perform(post("/api/v1/lost-items")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("黑色钱包"));
    }

    @Test
    void create_shouldReturn401WhenNoToken() throws Exception {
        String body = """
                {
                    "title": "黑色钱包",
                    "image_url": "https://example.com/img.jpg"
                }
                """;

        mockMvc.perform(post("/api/v1/lost-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或登录已过期"));
    }

    @Test
    void list_shouldReturnPagedResult() throws Exception {
        LostItem item = new LostItem();
        item.setId(1L);
        item.setTitle("黑色钱包");
        item.setCreateTime(LocalDateTime.now());

        PageImpl<LostItem> page = new PageImpl<>(
                List.of(item), PageRequest.of(0, 10), 1);

        when(service.findAll(eq("钱包"), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/lost-items")
                        .param("title", "钱包")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("黑色钱包"));
    }

    @Test
    void list_shouldFilterByMineWhenTokenValid() throws Exception {
        LostItem item = new LostItem();
        item.setId(1L);
        item.setTitle("黑色钱包");
        item.setCreateTime(LocalDateTime.now());

        PageImpl<LostItem> page = new PageImpl<>(
                List.of(item), PageRequest.of(0, 10), 1);

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("1");
        when(jwtUtil.parseToken("test-token")).thenReturn(claims);
        when(service.findAll(isNull(), eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/lost-items")
                        .param("mine", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("黑色钱包"));
    }

    @Test
    void list_shouldReturn401WhenMineTrueAndNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/lost-items")
                        .param("mine", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或登录已过期"));
    }

    @Test
    void list_shouldReturn401WhenMineTrueAndInvalidToken() throws Exception {
        when(jwtUtil.parseToken("bad-token"))
                .thenThrow(new io.jsonwebtoken.JwtException("invalid"));

        mockMvc.perform(get("/api/v1/lost-items")
                        .param("mine", "true")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或登录已过期"));
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        LostItem item = new LostItem();
        item.setId(1L);
        item.setTitle("黑色钱包");
        item.setDescription("在图书馆门口捡到");
        item.setLocation("图书馆");
        item.setContact("13800001111");
        item.setCreateTime(LocalDateTime.now());

        when(service.findById(1L)).thenReturn(item);

        mockMvc.perform(get("/api/v1/lost-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("黑色钱包"));
    }

    @Test
    void getById_shouldReturnErrorWhenNotFound() throws Exception {
        when(service.findById(999L))
                .thenThrow(new RuntimeException("失物信息不存在，id: 999"));

        mockMvc.perform(get("/api/v1/lost-items/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("失物信息不存在，id: 999"));
    }
}
