package com.uang.backend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GlmClientTest {

    @Test
    void chat_shouldThrowFriendlyErrorWhenApiKeyMissing() {
        GlmClient client = new GlmClient(
                "",                                    // api-key 为空
                "glm-4v-flash",
                "https://example.com/api/paas/v4",
                Duration.ofSeconds(5),
                Duration.ofSeconds(30),
                new ObjectMapper());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> client.chat(List.of()));
        assertEquals("AI 服务未配置（GLM_API_KEY 未设置）", ex.getMessage());
    }
}
