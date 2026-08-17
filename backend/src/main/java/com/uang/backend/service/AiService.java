package com.uang.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uang.backend.client.GlmClient;
import com.uang.backend.dto.DescribeImageRequest;
import com.uang.backend.dto.DescribeImageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 业务编排：限流 → 组装 prompt（seek/claim 差异化）→ 调用 GlmClient → 解析结果。
 * 模型调用已薄封装在 {@link GlmClient}，换模型不影响本类。
 */
@Service
public class AiService {

    private static final String SYSTEM_PROMPT = """
            你是失物招领平台的物品识别助手。请识别图片中的物品，输出简短的中文关键词短语，用于关键词匹配。
            要求：
            1. 只输出一个 JSON 对象：{"title": "物品名称（2-8字）", "description": "关键词短语，用空格分隔"}
            2. description 使用简短词语而非长句，例如 "黑色 皮质 拉链 男士 长款"
            3. 不要输出"这是一张图片"之类的废话，不要额外解释
            """;

    /** 拾物招领（claim）：重点描述外观特征，便于失主认领 */
    private static final String CLAIM_USER_PROMPT = """
            请识别图中的拾获物品，输出 JSON：{"title": "物品名称", "description": "外观特征关键词，空格分隔"}。
            重点描述便于失主认领的外观特征：颜色、材质、品牌、大小、新旧程度、特殊标记。
            """;

    /** 寻物启事（seek）：重点描述独有标识，便于与拾获物品匹配 */
    private static final String SEEK_USER_PROMPT = """
            请识别图中的丢失物品，输出 JSON：{"title": "物品名称", "description": "独有标识关键词，空格分隔"}。
            重点描述便于与拾获物品匹配的独有标识：品牌、颜色、尺寸、图案、刻字、破损痕迹。
            """;

    /** JSON 解析失败时的正则兜底 */
    private static final Pattern TITLE_PATTERN = Pattern.compile("\"title\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern DESC_PATTERN = Pattern.compile("\"description\"\\s*:\\s*\"([^\"]+)\"");

    private final GlmClient glmClient;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public AiService(GlmClient glmClient, RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.glmClient = glmClient;
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据图片生成物品名称建议与关键词描述
     * @param request 请求（imageBase64 必填；category 可选，seek/claim 差异化 prompt）
     * @param userId 调用用户 ID（限流维度）
     * @return 生成结果
     */
    public DescribeImageResult describeImage(DescribeImageRequest request, Long userId) {
        rateLimitService.check(userId);
        String userPrompt = "seek".equals(request.getCategory()) ? SEEK_USER_PROMPT : CLAIM_USER_PROMPT;
        List<Map<String, Object>> messages = buildMessages(userPrompt, request.getImageBase64());
        String raw = glmClient.chat(messages);
        return parseResult(raw);
    }

    /**
     * 组装 OpenAI 兼容消息：system + user（文本指令 + base64 图片）
     */
    private List<Map<String, Object>> buildMessages(String userPrompt, String imageBase64) {
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("role", "system");
        system.put("content", SYSTEM_PROMPT);

        Map<String, Object> textContent = new LinkedHashMap<>();
        textContent.put("type", "text");
        textContent.put("text", userPrompt);

        Map<String, Object> imageContent = new LinkedHashMap<>();
        imageContent.put("type", "image_url");
        Map<String, Object> imageUrl = new LinkedHashMap<>();
        imageUrl.put("url", "data:image/jpeg;base64," + imageBase64);
        imageContent.put("image_url", imageUrl);

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("role", "user");
        user.put("content", List.of(textContent, imageContent));

        return List.of(system, user);
    }

    /**
     * 解析模型输出为结构化结果：先 JSON 解析（容错 ```json 包裹），失败回退正则提取
     */
    private DescribeImageResult parseResult(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new RuntimeException("AI 生成内容为空，请重试或手动填写");
        }
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        try {
            JsonNode node = objectMapper.readTree(cleaned);
            String title = node.path("title").asText(null);
            String description = node.path("description").asText(null);
            if (StringUtils.hasText(title) && StringUtils.hasText(description)) {
                return new DescribeImageResult(title.trim(), description.trim());
            }
        } catch (Exception ignored) {
            // 非严格 JSON 时回退正则提取
        }
        Matcher titleMatcher = TITLE_PATTERN.matcher(cleaned);
        Matcher descMatcher = DESC_PATTERN.matcher(cleaned);
        if (titleMatcher.find() && descMatcher.find()) {
            return new DescribeImageResult(titleMatcher.group(1).trim(), descMatcher.group(1).trim());
        }
        throw new RuntimeException("AI 生成内容解析失败，请重试或手动填写");
    }
}
