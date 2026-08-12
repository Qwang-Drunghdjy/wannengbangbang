package com.uang.backend.controller;

import com.uang.backend.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查端点：供微信云托管健康检查使用，返回 HTTP 200
 */
@RestController
public class HealthController {

    /**
     * 健康检查
     * GET /health
     * @return 固定成功响应
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }
}
