package com.uang.backend.controller;

import com.uang.backend.config.AuthInterceptor;
import com.uang.backend.dto.DescribeImageRequest;
import com.uang.backend.dto.DescribeImageResult;
import com.uang.backend.dto.Result;
import com.uang.backend.service.AiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 辅助接口控制器（POST 需登录，由 AuthInterceptor 拦截）
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 根据图片自动生成物品名称建议与关键词描述
     * POST /api/v1/ai/describe
     * @param request 请求体（imageBase64 必填，category 可选）
     * @param httpRequest 请求（取拦截器注入的 userId 用于限流）
     * @return 生成的 title + description
     */
    @PostMapping("/describe")
    public Result<DescribeImageResult> describe(@Valid @RequestBody DescribeImageRequest request,
                                                HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute(AuthInterceptor.USER_ID_ATTR);
        return Result.success(aiService.describeImage(request, userId));
    }
}
