package com.uang.backend.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * GLM 视觉模型 HTTP 薄封装：只负责调用 open.bigmodel.cn 的 chat/completions 接口，
 * 鉴权、超时、错误提示集中在此。将来更换模型（如 Qwen-VL）只需修改本类。
 */
@Component
public class GlmClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;

    public GlmClient(@Value("${glm.api-key:}") String apiKey,
                     @Value("${glm.model:glm-4v-flash}") String model,
                     @Value("${glm.base-url:https://open.bigmodel.cn/api/paas/v4}") String baseUrl,
                     @Value("${glm.connect-timeout:5s}") Duration connectTimeout,
                     @Value("${glm.read-timeout:30s}") Duration readTimeout,
                     ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        // 连接超时通过 HttpClient 设置，读取超时通过 JdkClientHttpRequestFactory 设置
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * 调用 GLM-4V-Flash 对话补全接口
     * @param messages OpenAI 兼容消息列表（role + content）
     * @return 模型返回的 content 文本
     * @throws RuntimeException 未配置 Key / 调用失败 / 响应异常时抛出（信息友好）
     */
    public String chat(List<Map<String, Object>> messages) {
        if (!StringUtils.hasText(apiKey)) {
            throw new RuntimeException("AI 服务未配置（GLM_API_KEY 未设置）");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0.3,
                "max_tokens", 256,
                "response_format", Map.of("type", "json_object"));

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("AI 服务调用失败（" + e.getStatusCode() + "），请稍后再试");
        } catch (ResourceAccessException e) {
            throw new RuntimeException("AI 服务连接超时或不可达，请稍后再试");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("AI 服务响应异常，请稍后再试");
        }
    }
}
