package com.uang.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uang.backend.client.GlmClient;
import com.uang.backend.dto.DescribeImageRequest;
import com.uang.backend.dto.DescribeImageResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiServiceTest {

    private final GlmClient glmClient = mock(GlmClient.class);
    private final RateLimitService rateLimitService = mock(RateLimitService.class);
    private final AiService aiService = new AiService(glmClient, rateLimitService, new ObjectMapper());

    private DescribeImageRequest request(String category) {
        DescribeImageRequest req = new DescribeImageRequest();
        req.setImageBase64("aW1hZ2U=");
        req.setCategory(category);
        return req;
    }

    @Test
    void describeImage_shouldCheckRateLimitAndParseJson() {
        when(glmClient.chat(any()))
                .thenReturn("{\"title\":\"黑色钱包\",\"description\":\"黑色 皮质 拉链 男士\"}");

        DescribeImageResult result = aiService.describeImage(request("claim"), 1L);

        verify(rateLimitService).check(1L);
        assertEquals("黑色钱包", result.getTitle());
        assertEquals("黑色 皮质 拉链 男士", result.getDescription());
    }

    @Test
    void describeImage_shouldUseSeekPromptForSeek() {
        when(glmClient.chat(any()))
                .thenReturn("{\"title\":\"校园卡\",\"description\":\"白色 学生 一卡通 姓名贴纸\"}");

        aiService.describeImage(request("seek"), 1L);

        verify(glmClient).chat(argThat(messages -> userText(messages).contains("独有标识")));
    }

    @Test
    void describeImage_shouldUseClaimPromptByDefaultForInvalidCategory() {
        when(glmClient.chat(any()))
                .thenReturn("{\"title\":\"黑色钱包\",\"description\":\"黑色 皮质\"}");

        aiService.describeImage(request("invalid"), 1L);

        verify(glmClient).chat(argThat(messages -> userText(messages).contains("外观特征")));
    }

    @Test
    void describeImage_shouldHandleJsonCodeBlockWrapping() {
        when(glmClient.chat(any()))
                .thenReturn("```json\n{\"title\":\"黑色钱包\",\"description\":\"黑色 皮质 拉链 男士\"}\n```");

        DescribeImageResult result = aiService.describeImage(request("claim"), 1L);

        assertEquals("黑色钱包", result.getTitle());
        assertEquals("黑色 皮质 拉链 男士", result.getDescription());
    }

    @Test
    void describeImage_shouldFallbackToRegexWhenNotStrictJson() {
        when(glmClient.chat(any()))
                .thenReturn("结果如下：{\"title\": \"黑色钱包\", \"description\": \"黑色 皮质 拉链\"} 完毕");

        DescribeImageResult result = aiService.describeImage(request("claim"), 1L);

        assertEquals("黑色钱包", result.getTitle());
        assertEquals("黑色 皮质 拉链", result.getDescription());
    }

    @Test
    void describeImage_shouldThrowWhenUnparseable() {
        when(glmClient.chat(any())).thenReturn("无法识别图片内容");

        assertThrows(RuntimeException.class, () -> aiService.describeImage(request("claim"), 1L));
    }

    /** 提取 user 消息中的文本指令（messages[1] = user，content[0] = text） */
    @SuppressWarnings("unchecked")
    private String userText(List<Map<String, Object>> messages) {
        List<Map<String, Object>> content = (List<Map<String, Object>>) messages.get(1).get("content");
        return (String) content.get(0).get("text");
    }
}
