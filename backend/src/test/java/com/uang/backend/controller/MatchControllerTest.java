package com.uang.backend.controller;

import com.uang.backend.config.JwtUtil;
import com.uang.backend.dto.MatchResult;
import com.uang.backend.entity.FindItem;
import com.uang.backend.entity.LostItem;
import com.uang.backend.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchingService matchingService;

    // @WebMvcTest 会加载 WebMvcConfig + AuthInterceptor，需要 JwtUtil bean
    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void findMatches_shouldReturnResults() throws Exception {
        LostItem li = new LostItem();
        li.setId(1L);
        li.setTitle("黑色钱包");
        li.setDescription("在图书馆门口捡到");
        li.setLocation("图书馆");
        li.setContact("13800001111");
        li.setImageUrl("https://example.com/img.jpg");
        li.setCreateTime(LocalDateTime.now());

        MatchResult<LostItem> mr = new MatchResult<>(li, 0.85);
        when(matchingService.findMatches(1L, 3)).thenReturn(List.of(mr));

        mockMvc.perform(get("/api/v1/find-items/1/matches")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].score").value(0.85))
                .andExpect(jsonPath("$.data[0].item.title").value("黑色钱包"));
    }

    @Test
    void findMatches_shouldReturnEmptyListWhenNoMatches() throws Exception {
        when(matchingService.findMatches(1L, 3)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/find-items/1/matches")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void findMatches_shouldReturnErrorWhenFindItemNotFound() throws Exception {
        when(matchingService.findMatches(999L, 3))
                .thenThrow(new RuntimeException("寻物信息不存在，id: 999"));

        mockMvc.perform(get("/api/v1/find-items/999/matches")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("寻物信息不存在，id: 999"));
    }

    // ── findMatchesByLostItem（拾物 → 寻物）───────────────────

    @Test
    void findMatchesByLostItem_shouldReturnResults() throws Exception {
        FindItem fi = new FindItem();
        fi.setId(1L);
        fi.setTitle("黑色钱包");
        fi.setDescription("在图书馆门口丢失");
        fi.setLocation("图书馆");
        fi.setContact("13800001111");
        fi.setImageUrl("https://example.com/img.jpg");
        fi.setCreateTime(LocalDateTime.now());

        MatchResult<FindItem> mr = new MatchResult<>(fi, 0.75);
        when(matchingService.findMatchesByLostItem(2L, 3)).thenReturn(List.of(mr));

        mockMvc.perform(get("/api/v1/lost-items/2/matches")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].score").value(0.75))
                .andExpect(jsonPath("$.data[0].item.title").value("黑色钱包"));
    }

    @Test
    void findMatchesByLostItem_shouldReturnEmptyListWhenNoMatches() throws Exception {
        when(matchingService.findMatchesByLostItem(2L, 3)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lost-items/2/matches")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void findMatchesByLostItem_shouldReturnErrorWhenLostItemNotFound() throws Exception {
        when(matchingService.findMatchesByLostItem(999L, 3))
                .thenThrow(new RuntimeException("失物信息不存在，id: 999"));

        mockMvc.perform(get("/api/v1/lost-items/999/matches")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("失物信息不存在，id: 999"));
    }
}
